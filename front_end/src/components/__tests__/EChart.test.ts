import { describe, it, expect, vi } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import { defineComponent, h } from "vue";

const setOption = vi.fn();
const initMock = vi.fn(async () => ({ setOption }));

// lime-echart 依赖 uni 条件编译，单测中替换为桩组件
vi.mock("@/uni_modules/lime-echart/components/l-echart/l-echart.vue", () => ({
  default: defineComponent({
    name: "LEchartStub",
    emits: ["finished"],
    setup(_, { emit, expose }) {
      expose({ init: initMock });
      setTimeout(() => emit("finished"), 0);
      return () => h("div", { class: "l-echart-stub" });
    },
  }),
}));

import EChart from "../EChart.vue";

const pieOptions = {
  title: { text: "-¥200.00", subtext: "总支出" },
  series: [
    {
      type: "pie",
      radius: ["46%", "65%"],
      padAngle: 3,
      itemStyle: { borderRadius: 8 },
      data: [
        { name: "餐饮", value: 150 },
        { name: "购物", value: 50 },
      ],
    },
  ],
};

describe("EChart (lime-echart) Component", () => {
  it("initializes chart and applies rounded pie options", async () => {
    const wrapper = mount(EChart, {
      props: { options: pieOptions, height: 270 },
      global: {
        components: { view: { template: "<div :style=\"$attrs.style\"><slot /></div>" } },
      },
    });
    await new Promise((r) => setTimeout(r, 10));
    await flushPromises();

    expect(wrapper.find(".l-echart-stub").exists()).toBe(true);
    expect(initMock).toHaveBeenCalledTimes(1);
    expect(setOption).toHaveBeenCalledWith(pieOptions, true);
    const applied = setOption.mock.calls[0][0] as any;
    expect(applied.series[0].itemStyle.borderRadius).toBe(8);
    expect(applied.series[0].padAngle).toBe(3);
  });

  it("re-applies options when props change", async () => {
    setOption.mockClear();
    const wrapper = mount(EChart, {
      props: { options: pieOptions },
      global: { components: { view: { template: "<div><slot /></div>" } } },
    });
    await new Promise((r) => setTimeout(r, 10));
    await flushPromises();
    setOption.mockClear();

    const next = { ...pieOptions, title: { text: "+¥10.00", subtext: "总收入" } };
    await wrapper.setProps({ options: next });
    expect(setOption).toHaveBeenCalledWith(next, true);
  });
});
