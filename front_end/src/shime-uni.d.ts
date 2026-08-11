export {};

declare module "vue" {
  type Hooks = App.AppInstance & Page.PageInstance;
  interface ComponentCustomOptions extends Hooks {}
}

declare module "@qiun/ucharts" {
  const uCharts: any;
  export default uCharts;
}
