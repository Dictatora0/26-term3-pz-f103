#ifndef _music_app_H
#define _music_app_H

#include "gui.h"

//音乐播放控制器
typedef __packed struct
{  
	u8 *path;			//当前文件夹路径
	u8 *name;			//当前播放的MP3歌曲名字
	u16 curnamepos;		//当前的偏移

    u32 totsec ;		//整首歌时长,单位:秒
    u32 cursec ;		//当前播放时长 
    u32 bitrate;	   	//比特率(位速)
	u32 samplerate;		//采样率 
	u16 bps;			//位数,比如16bit,24bit,32bit
	
	u16 curindex;		//当前播放的音频文件索引
	u16 mfilenum;		//音乐文件数目	    
	u16 *mfindextbl;	//音频文件索引表
	
}__audiodev; 
extern __audiodev audiodev;	//音乐播放控制器

void Music_APP_Test(void);


#endif
