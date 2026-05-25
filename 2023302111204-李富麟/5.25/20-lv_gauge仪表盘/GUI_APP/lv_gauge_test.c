#include "lv_gauge_test.h"
#include "lvgl.h"
#include <stdio.h>


lv_style_t gauge_style;
lv_obj_t * gauge1;
lv_obj_t * label1;
//在keil中lv_color_t needle_colors[] = {LV_COLOR_BLUE, LV_COLOR_PURPLE};直接赋值报错,这是因为不接受LV_COLOR_MAKE形成的
//结构体赋值
lv_color_t needle_colors[2];//每一根指针的颜色,
int16_t speed_val = 0;


//任务回调函数
void task_cb(lv_task_t * task)
{
	static uint8_t is_add_dir = 1;//是否是速度增加的方向
	char buff[40];

	if(is_add_dir)
	{
			speed_val += 5;
			if(speed_val>=100)
				is_add_dir = 0;
	}else
	{
			speed_val -= 5;
			if(speed_val<=0)
				is_add_dir = 1;
	}
	//设置指针的数值
	lv_gauge_set_value(gauge1,0,speed_val);
	//把此速度显示在标签上,然后根据不同大小的数值显示出不同的文本颜色
	if(speed_val<60)
			sprintf(buff,"#5FB878 %d km/h#",speed_val);//显示绿色,代表安全
	else if(speed_val<90)
			sprintf(buff,"#FFB800 %d km/h#",speed_val);//显示黄色,代表警告
	else
			sprintf(buff,"#FF0000 %d km/h#",speed_val);//显示红色,代表危险
	lv_label_set_text(label1,buff);
}



//例程入口
void lv_gauge_test_start()
{
	lv_obj_t * scr = lv_scr_act();//获取当前活跃的屏幕对象

	//1.创建自定义样式
	lv_style_copy(&gauge_style, &lv_style_pretty_color);
	gauge_style.body.main_color = LV_COLOR_MAKE(0x5F,0xB8,0x78);//关键数值点之前的刻度线的起始颜色,为浅绿色
	gauge_style.body.grad_color =  LV_COLOR_MAKE(0xFF,0xB8,0x00);//关键数值点之前的刻度线的终止颜色,为浅黄色
	gauge_style.body.padding.left = 10;//每一条刻度线的长度
	gauge_style.body.padding.inner = 8;//数值标签与刻度线之间的距离
	gauge_style.body.border.color = LV_COLOR_MAKE(0x33,0x33,0x33);//中心圆点的颜色
	gauge_style.line.width = 3;//刻度线的宽度
	gauge_style.text.color = LV_COLOR_BLACK;//数值标签的文本颜色
	gauge_style.line.color = LV_COLOR_RED;//关键数值点之后的刻度线的颜色

	//2.创建一个gauge1仪表盘
	gauge1 = lv_gauge_create(scr, NULL);//创建仪表盘
	lv_obj_set_size(gauge1,200,200);//设置仪表盘的大小
	lv_gauge_set_style(gauge1,LV_GAUGE_STYLE_MAIN,&gauge_style);//设置样式
	lv_gauge_set_range(gauge1,0,100);//设置仪表盘的范围
	needle_colors[0] = LV_COLOR_BLUE;
	needle_colors[1] = LV_COLOR_PURPLE;
	lv_gauge_set_needle_count(gauge1,sizeof(needle_colors)/sizeof(needle_colors[0]),needle_colors);//设置指针的数量和其颜色
	lv_gauge_set_value(gauge1,0,speed_val);//设置指针1指向的数值,我们把指针1当作速度指针吧
	lv_gauge_set_value(gauge1,1,90);//设置指针2指向的数值,就让它指向关键数值点吧
	lv_gauge_set_critical_value(gauge1,90);//设置关键数值点
	lv_gauge_set_scale(gauge1,240,31,6);//设置角度,刻度线的数量,数值标签的数量
	lv_obj_align(gauge1,NULL,LV_ALIGN_CENTER,0,0);//设置与屏幕居中对齐

	//3.创建一个标签来显示指针1的数值
	label1 = lv_label_create(scr,NULL);
	lv_label_set_long_mode(label1,LV_LABEL_LONG_BREAK);//设置长文本模式
	lv_obj_set_width(label1,80);//设置固定的宽度
	lv_label_set_align(label1,LV_LABEL_ALIGN_CENTER);//设置文本居中对齐
	lv_label_set_style(label1,LV_LABEL_STYLE_MAIN,&lv_style_pretty);//设置样式
	lv_label_set_body_draw(label1,true);//使能背景重绘制
	lv_obj_align(label1,gauge1,LV_ALIGN_CENTER,0,60);//设置与gauge1的对齐方式
	lv_label_set_text(label1,"0 km/h");//设置文本
	lv_label_set_recolor(label1,true);//使能文本重绘色

	//4.创建一个任务来模拟速度指针的变化
	lv_task_create(task_cb,1000,LV_TASK_PRIO_MID,NULL);
}


