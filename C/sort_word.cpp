/*
 * sort_word.cpp
 *
 *  Created on: 2012-9-28
 *      Author: yewang
 *
 *      多线程  无锁  数字比较 预先分组
 *
 */

#include <stdint.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <ctype.h>
#include <time.h>
#include <errno.h>
#include <pthread.h>
#include <sys/time.h>
#include <locale.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/mman.h>
#include <string.h>


/** 逻辑分支跳转预测辅助. */
#if __GNUC__ > 2 || __GNUC_MINOR__ >= 96
    #define LIKELY(x)       __builtin_expect(!!(x),1)
    #define UNLIKELY(x)     __builtin_expect(!!(x),0)
    #define EXPECTED(x,y)   __builtin_expect((x),(y))
#else
    #define LIKELY(x)       (x)
    #define UNLIKELY(x)     (x)
    #define EXPECTED(x,y)   (x)
#endif


/**
 * 多线程  无锁  数字比较 预先分组
 *
 * 字符串的第一个字母作为 第一维度的下标
 * 在读取文件时将其切分成 多个小数组，后续可以进一步切分
 *
 * 最多24个字符，切分为 3个 uint64进行比较
 */

char    g_buf[ 1024 * 1024 * 3]     = {0};
char    g_out_buf[ 1024 * 1024 * 3] = {0};

int     g_thread_num                = 0;
int     g_word_num[256][256];                              // 每个分组有多少个字符串
char *  g_word_arr[256][256][5000];                        // 所有的字符串


const int DARRAR_INCREMENT[] = {1, 4, 10, 23, 57, 132, 301, 701, 1750, 4023, 9258, 21293, 48974, 112640};


static inline int
ac_strcmp(const char* str1, const char* str2)
{
    register int ret = 0;

    while(!(ret = *(unsigned char*)str1-*(unsigned char*)str2) && *str1)
    {
        ++str1;
        ++str2;
    }

    return ret;
}




static inline void
shell_sort( char ** array, register int num )
{
    register int incIdx = -1;

    /* 找到合适的步长 */
    for ( register int i = (sizeof(DARRAR_INCREMENT)/sizeof(int) - 1); i >= 0; --i)
    {
        if (DARRAR_INCREMENT[i] < num )
        {
            incIdx = i;
            break;
        }
    }

    /* 开始循环比较 */
    while (incIdx >= 0)
    {
        register int increment = DARRAR_INCREMENT[incIdx];
        register int startInc  = increment;

        for (register int i = startInc; i < num; ++i)
        {
            register int     j   = i;
            register char *  tmp = array[i];

            while ((j >= startInc) && (ac_strcmp(tmp, array[j - increment]) < 0))
            {
                array[j] = array[j - increment];

                j -= increment;
            }

            array[j] = tmp;
        }

        --incIdx;                                          /* 换一种步长 */
    }
}


static inline
void * pthread_worker(void * param)
{
    register int  idx = (int64_t)param;                    // 标示自己要处理的位置

    for (register  size_t i = 0; i < 256; ++i )
    {
        register int  cnt = i * 256 ;

        for (register  size_t j = 0; j < 256; ++j )
        {
            if ( idx != ( cnt + j ) % g_thread_num )
                continue;

            if ( 0 == g_word_num[ i ][ j ] )
                continue;

            shell_sort( g_word_arr[ i ][ j ], g_word_num[ i ][ j ] );
        }
    }

    return 0;
}



/** 主程序入口  */
int main( int argc, char *argv[] )
{
    struct timeval start_time;
    struct timeval end_time;

    pthread_t   thread_arr[ 32 ] = {0};                    // 线程句柄
    char      * word_file        = argv[ 2 ];
    int         file_handler     = -1;
    size_t      file_length      = 0;
    char      * map_base         = NULL;
    bool        is_new_str       = true;

    g_thread_num = atoi( argv[ 1 ] );

    gettimeofday(&start_time, NULL);

    memset( g_word_num, 0, sizeof( g_word_num ) );

    // mmap读出文件，进行预读
    file_handler = ::open( word_file, O_RDWR, 0644 );

    struct stat statInfo;
    fstat( file_handler, &statInfo );

    file_length = statInfo.st_size;

    map_base = (char *)mmap( NULL, file_length, PROT_READ|PROT_WRITE, MAP_SHARED, file_handler, 0);
    madvise( map_base, file_length, MADV_SEQUENTIAL );

    // 预处理数据, mmap映射，顺序扫， 替换\n成\0
    for ( register size_t i = 0; i < file_length; ++i )
    {
        register int ch = map_base[ i ];

        if (UNLIKELY( is_new_str == true ))                // 第一个字符
        {
            register int ch2 = map_base[ i + 1 ];

            g_word_arr[ ch ][ ch2 ][ g_word_num[ ch ][ ch2 ] ] = map_base + i + 2 ;

            ++(g_word_num[ ch ][ ch2 ]);

            is_new_str = false;
        }

        if (UNLIKELY( '\0' == ch ))
            is_new_str = true;
    }

    // 启动多个线程
    for ( int i = 0; i < g_thread_num; ++i )
    {
        pthread_create( &thread_arr[ i ], NULL, pthread_worker, (void *)i );
    }

    // 等待工作线程终止
    for ( int i = 0; i < g_thread_num; ++i )
    {
        pthread_join( thread_arr[ i ], NULL );
    }

    // 挨个输出每个数组
    register int  write_pos = 0;

    for (register  size_t i = 0; i < 256; ++i )
    {
        for (register  size_t j = 0; j < 256; ++j )
        {
            register int word_num = g_word_num[ i ][ j ];

            for ( register  int k = 0; k < word_num; ++k )
            {
                g_out_buf[ write_pos++ ] = (char)i;
                g_out_buf[ write_pos++ ] = (char)j;

                register char * str = g_word_arr[ i ][ j ][ k ];

                register int pos = 0;

                while( str[ pos ] != '\0' )
                {
                    g_out_buf[ write_pos++ ] = str[ pos++ ];
                }

                ++write_pos;
                //g_out_buf[ write_pos++ ] = '\0';
            }
        }
    }

    setvbuf( stdout, g_buf, _IOFBF, sizeof( g_buf ));
    fwrite( g_out_buf, 1, write_pos, stdout );

    gettimeofday(&end_time, NULL);

    // 释放资源，不做了
    fprintf(stderr, "time consumed: %lu:s %lu:us \n",
                       end_time.tv_sec  - start_time.tv_sec,
                       (end_time.tv_usec - start_time.tv_usec) );

    return 0;
}

