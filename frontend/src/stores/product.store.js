import { defineStore } from 'pinia';

const generateProducts = (count) => {
  const products = [];
  const titles = [
    '九成新山地自行车', '考研数学全套资料', '罗技G102鼠标', '入门级民谣吉他',
    '闲置的iPad Air 3', '宿舍用小冰箱', '品牌篮球', '几乎全新的羽毛球拍',
    '九五新戴尔显示器', '二手电吉他效果器', '宿舍小电锅', '专业网球拍'
  ];
  const sellers = [
    { nickname: '学长小张', avatarUrl: 'https://i.pravatar.cc/40?u=1' },
    { nickname: '考研上岸的学姐', avatarUrl: 'https://i.pravatar.cc/40?u=2' },
    { nickname: '电竞爱好者', avatarUrl: 'https://i.pravatar.cc/40?u=3' },
    { nickname: '文艺青年', avatarUrl: 'https://i.pravatar.cc/40?u=4' },
  ];

  for (let i = 1; i <= count; i++) {
    products.push({
      id: i,
      title: titles[i % titles.length],
      description: '这是一个商品的简短描述，用于展示在卡片上。',
      price: (Math.random() * 300 + 20).toFixed(2),
      // 使用picsum.photos生成不同尺寸的随机图片
      imageUrl: `https://picsum.photos/400/${Math.floor(Math.random() * 200) + 300}?random=${i}`,
      seller: sellers[i % sellers.length]
    });
  }
  return products;
};

export const useProductStore = defineStore('products', {
  state: () => ({
    products: generateProducts(20), // 生成20个商品数据以便更好地展示瀑布流
    categories: [
        { id: 1, name: '电子产品', href: '#' },
        { id: 2, name: '学习书籍', href: '#' },
        { id: 3, name: '生活用品', href: '#' },
        { id: 4, name: '服饰鞋包', href: '#' },
        { id: 5, name: '运动户外', href: '#' },
    ]
  }),
  getters: {
    getProductById: (state) => (id) => {
      return state.products.find(p => p.id === parseInt(id));
    }
  },
  actions: {
    // 后续添加从API获取数据的actions
  },
});
