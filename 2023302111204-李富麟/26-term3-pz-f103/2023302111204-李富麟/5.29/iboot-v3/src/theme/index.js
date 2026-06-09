export function IVTheme(headerBackgroundColor, headerColor, logoBackgroundColor) {
    this.headerColor = headerColor;
    this.logoBackgroundColor = logoBackgroundColor;
    this.headerBackgroundColor = headerBackgroundColor;
}

// 默认样式
const defaultStyle = 'light';
// 纯白主题颜色配置
const light = new IVTheme('#ffffff', 'rgba(0, 0, 0, 0.88)', '#ffffff');
const dark = new IVTheme('#ffffff', 'rgba(0, 0, 0, 0.88)', '#001529');
const themeMaps = {light, dark}
export function IVThemeConfig() {

    this.style = 'light'; // dark

    /**
     * 配置Drawer开关状态
     * @type {boolean}
     */
    this.open = false;

    /**
     * @type {IVTheme}
     */
    this.theme = light;

    /**
     * 头字体颜色
     */
    this.headerColor = this.theme.headerColor;

    /**
     * 头背景颜色
     */
    this.headerBackgroundColor = this.theme.headerBackgroundColor;

    /**
     * logo栏背景颜色
     */
    this.logoBackgroundColor = this.theme.logoBackgroundColor;

    /**
     * 主颜色
     * @type {string}
     */
    this.colorPrimary = "#1677FF";

    /**
     * 候选主题颜色
     * @type {[{value: string}, {value: string}, {value: string}, {value: string}]}
     */
    this.candidateColorPrimary = [{value: '#1677FF'}, {value: '#00B96B'}, {value: '#722ED1'}, {value: '#FA541C'}];

    /**
     * 主颜色底色
     */
    this.colorBackgroundPrimary = this.colorPrimary

    /**
     * 顶栏用主题色
     * @type {boolean}
     */
    this.headerColorPrimary = false;

    /**
     * 切换配置状态
     */
    this.switchOpen = function () {
        this.open = !this.open;
    }

    /**
     * 切换主题风格
     * @param style {String}
     */
    this.switchStyle = function (style) {
        this.style = style;
        this.theme = themeMaps[style];
        this.switchColorPrimary(this.colorPrimary);
    }

    /**
     * 切换主颜色
     * @param value
     */
    this.switchColorPrimary = function (value) {
        this.colorPrimary = value;
        this.colorBackgroundPrimary = (value + "17");
        this.switchHeaderColorPrimary(this.headerColorPrimary);
    }

    /**
     * 头部使用主题色作为背景
     */
    this.switchHeaderColorPrimary = function (value) {
        this.headerColorPrimary = value;
        if(this.headerColorPrimary) {
            this.headerColor = '#ffffff'
            this.logoBackgroundColor = this.headerBackgroundColor = this.colorPrimary;
        } else {
            this.headerColor = this.theme.headerColor;
            this.logoBackgroundColor = this.theme.logoBackgroundColor;
            this.headerBackgroundColor = this.theme.headerBackgroundColor;
        }

        // 存储配置信息
        localStorage.setItem("localThemeConfig", JSON.stringify(this));
    }

    /**
     * 加载配置
     */
    this.loadingConfigFormStorage = function () {
        let localThemeConfig = localStorage.getItem("localThemeConfig");

        if(typeof localThemeConfig === 'string') {
            localThemeConfig = JSON.parse(localThemeConfig);
            this.colorPrimary = localThemeConfig.colorPrimary;
            this.headerColorPrimary = localThemeConfig.headerColorPrimary;
        } else {
            localThemeConfig = {style: defaultStyle};
            localStorage.setItem("localThemeConfig", JSON.stringify(this));
        }

        // 切换样式
        this.switchStyle(localThemeConfig.style);
    }

    this.loadingConfigFormStorage();

}

export default new IVThemeConfig();
