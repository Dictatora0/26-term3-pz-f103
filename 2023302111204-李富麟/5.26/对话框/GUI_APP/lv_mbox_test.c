#include "lv_mbox_test.h"
#include "lvgl.h"

static lv_style_t bg_style;
static lv_style_t btnm_bg_style;
static lv_style_t btn_rel_style;
static lv_style_t btn_pr_style;
static lv_obj_t * open_btn;
static lv_obj_t * mbox1;
static const char * const btns_map[] = {"#5FB878 OK#", "\n", "#ff0000 Close#", ""};

static void event_handler(lv_obj_t * obj, lv_event_t event);
static void mbox_set_msg_recolor(lv_obj_t * mbox, bool en);
static lv_obj_t * mbox_create(lv_obj_t * parent);

static void event_handler(lv_obj_t * obj, lv_event_t event)
{
    uint16_t btn_id;

    if(obj == open_btn) {
        if(event == LV_EVENT_RELEASED) {
            mbox1 = mbox_create(lv_scr_act());
        }
    } else if(obj == mbox1) {
        if(event == LV_EVENT_VALUE_CHANGED) {
            btn_id = lv_mbox_get_active_btn(obj);

            if(btn_id == 0) {
                lv_mbox_start_auto_close(obj, 0);
            } else if(btn_id == 1) {
                lv_mbox_start_auto_close(obj, 1000);
            }
        }
    }
}

static void mbox_set_msg_recolor(lv_obj_t * mbox, bool en)
{
    lv_mbox_ext_t * ext = lv_obj_get_ext_attr(mbox);
    lv_label_set_recolor(ext->text, en);
}

static lv_obj_t * mbox_create(lv_obj_t * parent)
{
#define MBOX_WIDTH      220
#define MBOX_BTN_HEIGHT 30
#define MBOX_BTN_NUM    2

    lv_obj_t * mbox = lv_mbox_create(parent, NULL);
    lv_obj_t * btnm_of_mbox;

    mbox_set_msg_recolor(mbox, true);
    lv_mbox_set_text(mbox, "#007AFF Dialog#\nTouch OK or Close");
    lv_mbox_add_btns(mbox, (const char **)btns_map);
    lv_mbox_set_recolor(mbox, true);
    lv_obj_set_width(mbox, MBOX_WIDTH);
    lv_obj_align(mbox, NULL, LV_ALIGN_CENTER, 0, 0);
    lv_obj_set_event_cb(mbox, event_handler);
    lv_mbox_set_style(mbox, LV_MBOX_STYLE_BG, &bg_style);
    lv_mbox_set_style(mbox, LV_MBOX_STYLE_BTN_BG, &btnm_bg_style);
    lv_mbox_set_style(mbox, LV_MBOX_STYLE_BTN_REL, &btn_rel_style);
    lv_mbox_set_style(mbox, LV_MBOX_STYLE_BTN_PR, &btn_pr_style);

    btnm_of_mbox = lv_mbox_get_btnm(mbox);
    lv_obj_set_size(btnm_of_mbox, MBOX_WIDTH, MBOX_BTN_HEIGHT * MBOX_BTN_NUM);

    return mbox;
}

void lv_mbox_test_start(void)
{
    lv_obj_t * scr = lv_scr_act();
    lv_obj_t * label1;

    lv_style_copy(&bg_style, &lv_style_plain_color);
    bg_style.body.main_color = LV_COLOR_MAKE(250, 250, 250);
    bg_style.body.grad_color = bg_style.body.main_color;
    bg_style.body.radius = 10;
    bg_style.body.border.width = 1;
    bg_style.body.border.color = LV_COLOR_MAKE(150, 150, 150);
    bg_style.body.shadow.color = bg_style.body.border.color;
    bg_style.body.shadow.width = 6;
    bg_style.body.padding.top = 10;
    bg_style.body.padding.bottom = 0;
    bg_style.body.padding.inner = 10;
    bg_style.text.color = LV_COLOR_BLACK;

    lv_style_copy(&btnm_bg_style, &lv_style_transp_tight);
    btnm_bg_style.body.padding.top = 0;
    btnm_bg_style.body.padding.left = 0;
    btnm_bg_style.body.padding.right = 0;
    btnm_bg_style.body.padding.bottom = 0;
    btnm_bg_style.body.padding.inner = 0;

    lv_style_copy(&btn_rel_style, &lv_style_transp);
    btn_rel_style.body.border.part = LV_BORDER_TOP;
    btn_rel_style.body.border.width = 1;
    btn_rel_style.body.border.color = bg_style.body.border.color;

    lv_style_copy(&btn_pr_style, &btn_rel_style);
    btn_pr_style.body.opa = LV_OPA_COVER;
    btn_pr_style.body.border.part = LV_BORDER_FULL;
    btn_pr_style.body.main_color = LV_COLOR_MAKE(200, 200, 200);
    btn_pr_style.body.grad_color = btn_pr_style.body.main_color;

    open_btn = lv_btn_create(scr, NULL);
    lv_obj_set_size(open_btn, 150, 60);
    lv_obj_set_event_cb(open_btn, event_handler);
    label1 = lv_label_create(open_btn, NULL);
    lv_label_set_text(label1, "Open mbox");
    lv_obj_align(open_btn, NULL, LV_ALIGN_IN_BOTTOM_MID, 0, -20);

    mbox1 = mbox_create(scr);
}
