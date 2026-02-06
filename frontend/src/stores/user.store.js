import { defineStore } from 'pinia';

const availableSupportAgents = [
    { id: 'support-a', name: '官方客服-小A', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
    { id: 'support-b', name: '官方客服-小B', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
    { id: 'support-c', name: '官方客服-小C', avatarUrl: 'https://gw.alicdn.com/tfs/TB1D0_9q97PL1JjSZFjXXcR_XXa-108-108.png' },
];

export const useUserStore = defineStore('user', {
  state: () => ({
    profile: {
      username: 'testuser-final',
      nickname: '爱学习的小明',
      email: 'test@example.com',
      phone: '138****1234',
      isCertified: false,
      bio: '一个爱学习、爱生活、爱分享的同学。',
      gender: '男',
      avatarUrl: '',
      stats: {
        likes: 128,
        following: 34,
        followers: 56,
      },
      balance: 1234.56,
      shippingInfo: {
        name: '爱学习的小明',
        phone: '138****1234',
        address: 'XX大学 XX校区 XX宿舍 101室',
      },
    },
    myProducts: [
      {
        id: 101,
        title: '九成新 Switch 游戏机（附带两个游戏）',
        price: '1500.00',
        imageUrl: 'https://picsum.photos/400/300?random=101',
        status: 'published',
      },
      {
        id: 102,
        title: '几乎全新的羽毛球拍，仅用过一次',
        price: '80.00',
        imageUrl: 'https://picsum.photos/400/300?random=102',
        status: 'published',
      },
      {
        id: 103,
        title: '已出 - 品牌电脑椅',
        price: '200.00',
        imageUrl: 'https://picsum.photos/400/300?random=103',
        status: 'delisted',
      },
    ],
    drafts: [
      {
        id: 201,
        title: '（草稿）准备出的一套专业书籍',
        price: '120.00',
        imageUrl: 'https://picsum.photos/400/300?random=201',
      }
    ],
    favoriteProducts: [
      {
        id: 1,
        title: '九成新山地自行车',
        price: '288.00',
        imageUrl: 'https://picsum.photos/400/300?random=1',
        status: 1, // 1 for valid
        seller: { nickname: '张三', avatarUrl: 'https://i.pravatar.cc/40?u=1' },
      },
      {
        id: 2,
        title: '考研数学全套复习资料',
        price: '60.00',
        imageUrl: 'https://picsum.photos/400/300?random=2',
        status: 1,
        seller: { nickname: '李四', avatarUrl: 'https://i.pravatar.cc/40?u=2' },
      },
      {
        id: 3,
        title: '一个几乎没用过的篮球',
        price: '50.00',
        imageUrl: 'https://picsum.photos/400/300?random=3',
        status: 2, // 2 for invalid/delisted
        seller: { nickname: '王五', avatarUrl: 'https://i.pravatar.cc/40?u=3' },
      },
    ],
    conversations: [
      {
        id: 1,
        user: { name: '买家A', avatarUrl: 'https://i.pravatar.cc/40?u=10' },
        messages: [
          { id: 1, text: '你好，这个商品还在吗？', sender: 'user', timestamp: '2023-10-27 10:00', read: true },
          { id: 2, text: '在的，可以直接拍。', sender: 'me', timestamp: '2023-10-27 10:01', read: true },
        ],
        unreadCount: 0,
        timestamp: '2023-10-27 10:01',
        lastMessage: '在的，可以直接拍。',
      },
      {
        id: 2,
        user: { name: '买家B', avatarUrl: 'https://i.pravatar.cc/40?u=11' },
        messages: [
          { id: 1, text: '可以便宜一点吗？', sender: 'user', timestamp: '2023-10-28 11:00', read: false },
        ],
        unreadCount: 1,
        timestamp: '2023-10-28 11:00',
        lastMessage: '可以便宜一点吗？',
      },
      {
        id: 3,
        user: { name: '考研上岸的学姐', avatarUrl: 'https://i.pravatar.cc/40?u=2' },
        messages: [
          { id: 1, text: '学妹，你的资料收到了吗？', sender: 'user', timestamp: '2023-10-29 12:00', read: true },
          { id: 2, text: '收到了，谢谢学姐！', sender: 'me', timestamp: '2023-10-29 12:01', read: true },
          { id: 3, text: '不客气，加油！', sender: 'user', timestamp: '2023-10-29 12:02', read: false },
        ],
        unreadCount: 1,
        timestamp: '2023-10-29 12:02',
        lastMessage: '不客气，加油！',
      },
    ],
    supportConversations: [],
    notifications: [],
    soldOrders: [
      {
        id: 'S001',
        buyer: { name: '买家A', avatarUrl: 'https://i.pravatar.cc/40?u=10' },
        seller: { name: '爱学习的小明' },
        status: '待付款',
        totalPrice: '100.00',
        product: { id: 1, title: '【我卖出的】待付款的商品', imageUrl: 'https://picsum.photos/400/300?random=21', price: '100.00' },
        shippingAddress: 'XX大学 XX宿舍 101室',
        orderNumber: '1111111111',
        createTime: '2023-10-25 10:00',
        priceDetails: { productTotal: '100.00', shippingFee: '0.00' },
      },
      {
        id: 'S002',
        buyer: { name: '买家B', avatarUrl: 'https://i.pravatar.cc/40?u=11' },
        seller: { name: '爱学习的小明' },
        status: '待发货',
        totalPrice: '288.00',
        product: { id: 2, title: '【我卖出的】待发货的商品', imageUrl: 'https://picsum.photos/400/300?random=10', price: '288.00' },
        shippingAddress: 'XX大学 XX宿舍 102室',
        orderNumber: '2222222222',
        createTime: '2023-10-20 10:00',
        priceDetails: { productTotal: '288.00', shippingFee: '0.00' },
      },
      {
        id: 'S003',
        buyer: { name: '买家C', avatarUrl: 'https://i.pravatar.cc/40?u=12' },
        seller: { name: '爱学习的小明' },
        status: '退款中',
        totalPrice: '75.00',
        product: { id: 3, title: '【我卖出的】退款中的商品', imageUrl: 'https://picsum.photos/400/300?random=22', price: '75.00' },
        shippingAddress: 'XX大学 XX宿舍 103室',
        orderNumber: '3333333333',
        createTime: '2023-10-26 11:00',
        payTime: '2023-10-26 11:05',
        shipTime: '2023-10-26 12:00',
        previousStatus: '待收货',
        priceDetails: { productTotal: '75.00', shippingFee: '0.00' },
        refundInfo: {
            rejectionCount: 0,
            history: [
                {
                    time: '2023-10-27 09:00',
                    text: '您发起了退款申请，等待卖家处理。',
                    reason: '不想要了',
                    description: '买错了，重新下单'
                }
            ]
        }
      },
    ],
    boughtOrders: [
      {
        id: 'B001',
        seller: { name: '考研上岸的学姐', avatarUrl: 'https://i.pravatar.cc/40?u=2' },
        buyer: { name: '爱学习的小明' },
        status: '已完成',
        totalPrice: '60.00',
        buyerReview: {
            initial: { rating: 5, comment: '资料很棒，谢谢学姐！' },
            additional: { comment: '用了一段时间，感觉真的很不错，推荐购买！', time: '2023-11-01' }
        },
        sellerReview: {
            initial: { comment: '感谢学妹的信任，祝你考研顺利！' }
        },
        product: { id: 2, title: '【我买到的】待评价的商品', imageUrl: 'https://picsum.photos/400/320?random=15', price: '60.00' },
        shippingAddress: 'XX大学 XX宿舍 303室',
        orderNumber: '1234567893',
        createTime: '2023-10-17 13:00',
        priceDetails: { productTotal: '60.00', shippingFee: '0.00' },
      },
      {
        id: 'B002',
        seller: { name: '健身达人', avatarUrl: 'https://i.pravatar.cc/40?u=12' },
        buyer: { name: '爱学习的小明' },
        status: '待付款',
        totalPrice: '120.00',
        product: { id: 9, title: '【我买到的】待付款的商品', imageUrl: 'https://picsum.photos/400/310?random=18', price: '120.00' },
        shippingAddress: 'XX大学 XX宿舍 404室',
        orderNumber: '1234567894',
        createTime: '2023-10-16 14:00',
        priceDetails: { productTotal: '120.00', shippingFee: '0.00' },
      },
      {
        id: 'B003',
        seller: { name: '数码爱好者', avatarUrl: 'https://i.pravatar.cc/40?u=13' },
        buyer: { name: '爱学习的小明' },
        status: '待评价',
        totalPrice: '350.00',
        product: { id: 10, title: '【我买到的】待评价的商品 - 蓝牙耳机', imageUrl: 'https://picsum.photos/400/310?random=19', price: '350.00' },
        shippingAddress: 'XX大学 XX宿舍 404室',
        orderNumber: '1234567895',
        createTime: '2023-11-02 10:00',
        priceDetails: { productTotal: '350.00', shippingFee: '0.00' },
      },
      {
        id: 'B004',
        seller: { name: '电子书专卖', avatarUrl: 'https://i.pravatar.cc/40?u=14' },
        buyer: { name: '爱学习的小明' },
        status: '退款中',
        totalPrice: '25.00',
        product: { id: 11, title: '【我买到的】退款中的商品 - PDF电子书', imageUrl: 'https://picsum.photos/400/310?random=20', price: '25.00' },
        shippingAddress: '电子商品无需地址',
        orderNumber: '1234567896',
        createTime: '2023-11-03 10:00',
        payTime: '2023-11-03 10:01',
        previousStatus: '待发货',
        priceDetails: { productTotal: '25.00', shippingFee: '0.00' },
        refundInfo: {
            rejectionCount: 0,
            history: [
                {
                    time: '2023-11-03 11:00',
                    text: '您发起了退款申请，等待卖家处理。',
                    reason: '不想要了',
                    description: '买错了'
                }
            ]
        }
      },
    ],
    allUsers: [
        { id: 1, username: 'testuser1', nickname: '张三', email: 'zhangsan@test.com', status: '正常' },
        { id: 2, username: 'testuser2', nickname: '李四', email: 'lisi@test.com', status: '正常' },
        { id: 3, username: 'testuser3', nickname: '王五', email: 'wangwu@test.com', status: '禁用' },
        { id: 4, username: 'testuser4', nickname: '赵六', email: 'zhaoliu@test.com', status: '正常' },
    ],
    allProducts: [
        { id: 1, title: '九成新山地自行车', seller: '张三', category: '交通工具', price: '288.00', status: '在售', reviews: [
            { id: 1, user: { name: '骑行爱好者', avatarUrl: 'https://i.pravatar.cc/40?u=a' }, comment: '车子很棒，骑起来很轻快，推荐！', reply: '感谢支持！' },
            { id: 2, user: { name: '小透明', avatarUrl: 'https://i.pravatar.cc/40?u=b' }, comment: '卖家发货很快，车子和描述的一样。' },
        ] },
        { id: 2, title: '考研数学全套复习资料', seller: '李四', category: '书籍资料', price: '60.00', status: '在售' },
        { id: 3, title: '一个几乎没用过的篮球', seller: '王五', category: '体育用品', price: '50.00', status: '已下架' },
        { id: 4, title: '八成新显示器', seller: '张三', category: '电子产品', price: '450.00', status: '在售' },
    ],

  }),
  getters: {
    allConversations: (state) => [...state.conversations, ...state.supportConversations],
  },
  actions: {
    updateProfile(newProfileData) {
        this.profile = { ...this.profile, ...newProfileData };
    },
    recharge(amount) {
        this.profile.balance += amount;
    },
    deductBalance(amount) {
        if (this.profile.balance >= amount) {
            this.profile.balance -= amount;
            return true;
        }
        return false;
    },
    sendNotification(notification) {
        const newNotification = {
            id: Date.now(),
            title: notification.title,
            content: notification.content,
            timestamp: new Date().toLocaleString('zh-CN'),
        };
        this.notifications.unshift(newNotification);
    },
    toggleUserStatus(userId) {
        const user = this.allUsers.find(u => u.id === userId);
        if (user) {
            user.status = user.status === '正常' ? '禁用' : '正常';
        }
    },
    adminToggleProductStatus(productId) {
        const product = this.allProducts.find(p => p.id === productId);
        if (product) {
            product.status = product.status === '在售' ? '已下架' : '在售';
        }
    },

    getOrCreateConversationBySeller(seller) {
        let conversation = this.conversations.find(c => c.user.name === seller.name);
        if (!conversation) {
            const newConversationId = this.conversations.length + 1;
            conversation = {
                id: newConversationId,
                user: seller,
                messages: [],
                unreadCount: 0,
            };
            this.conversations.push(conversation);
        }
        return conversation.id;
    },

    markConversationAsRead(conversationId) {
        const conversation = this.allConversations.find(c => c.id === conversationId);
        if (conversation) {
            conversation.unreadCount = 0;
        }
    },

    updateOrderStatus(orderId, newStatus, details) {
        const order = [...this.soldOrders, ...this.boughtOrders].find(o => o.id === orderId);
        if (!order) return;

        if (newStatus === '退款中' && details) {
            order.previousStatus = order.status; // Save the status before refund
            order.status = '退款中';
            order.refundInfo = {
                rejectionCount: 0,
                history: [
                    {
                        time: new Date().toLocaleString('zh-CN'),
                        text: '您发起了退款申请，等待卖家处理。',
                        reason: details.reason,
                        description: details.description
                    }
                ]
            };
        } else {
            order.status = newStatus;
            if (newStatus === '退款成功') {
                if (!order.refundInfo) {
                    order.refundInfo = { rejectionCount: 0, history: [] };
                }
                order.refundInfo.history.push({
                    time: new Date().toLocaleString('zh-CN'),
                    text: '卖家已同意退款，退款已完成。'
                });
            }
        }
    },

    rejectRefund(orderId, reason) {
        const order=[...this.soldOrders,...this.boughtOrders].find(o=>o.id===orderId);
        if(order && order.status==='退款中' && order.refundInfo){
            order.refundInfo.rejectionCount+=1;
            order.refundInfo.history.push({
                time:new Date().toLocaleString('zh-CN'),
                text:'卖家拒绝了退款申请，等待买家维权',
                isRejection:true,
                reason,
                description:'',
                rejectionTimestamp: Date.now()
            });
        }
    },

    forceRefundByAdmin(orderId) {
        const order = [...this.soldOrders, ...this.boughtOrders].find(o => o.id === orderId);
        if (order && order.status === '退款中') {
            order.status = '退款成功';
            if (!order.refundInfo) {
                order.refundInfo = { rejectionCount: 0, history: [] };
            }
            order.refundInfo.history.push({
                time: new Date().toLocaleString('zh-CN'),
                text: '管理员已强制退款，退款已完成。',
            });
        }
    },

    forceCloseRefundByAdmin(orderId) {
        const order = [...this.soldOrders, ...this.boughtOrders].find(o => o.id === orderId);
        if (order && order.status === '退款中') {
            order.status = '退款失败';
            if (!order.refundInfo) {
                order.refundInfo = { rejectionCount: 0, history: [] };
            }
            order.refundInfo.history.push({
                time: new Date().toLocaleString('zh-CN'),
                text: '管理员已强制关单，本次退款申请失败。',
            });
        }
    },

    // 保留旧逻辑占位，暂不使用
    forceRefundByAdmin_OLD(orderId) {
        const order = [...this.soldOrders, ...this.boughtOrders].find(o => o.id === orderId);
        if (order && order.status === '退款中') {
            order.status = '退款成功';
            if (!order.refundInfo) {
                order.refundInfo = { rejectionCount: 0, history: [] };
            }
            order.refundInfo.history.push({
                time: new Date().toLocaleString('zh-CN'),
                text: '管理员已强制退款，退款已完成。',
            });
        }
    },

    submitBuyerReview(orderId, review) {
        const order = this.boughtOrders.find(o => o.id === orderId);
        if (order) {
            order.buyerReview = { initial: review };
            order.status = '已完成';
        }
    },

    submitSellerReview(orderId, comment) {
        const order = this.soldOrders.find(o => o.id === orderId);
        if (order) {
            order.sellerReview = { initial: { comment } };
            order.status = '已完成';
        }
    },

    deleteOrder(orderId) {
        let index = this.boughtOrders.findIndex(o => o.id === orderId);
        if (index !== -1) {
            this.boughtOrders.splice(index, 1);
            return;
        }
        index = this.soldOrders.findIndex(o => o.id === orderId);
        if (index !== -1) {
            this.soldOrders.splice(index, 1);
        }
    },

    deleteOrder(orderId) {
        let index = this.boughtOrders.findIndex(o => o.id === orderId);
        if (index !== -1) {
            this.boughtOrders.splice(index, 1);
            return;
        }
        index = this.soldOrders.findIndex(o => o.id === orderId);
        if (index !== -1) {
            this.soldOrders.splice(index, 1);
        }
    },

    certifyCampus(certificationData) {
        // In a real app, this would involve an API call.
        // Here, we just simulate the success.
        console.log('【模拟校园认证】', certificationData);
        this.profile.isCertified = true;
    },

    getOrCreateRandomSupportConversation() {
        const supportAgent = availableSupportAgents[Math.floor(Math.random() * availableSupportAgents.length)];
        let supportConversation = this.supportConversations.find(c => c.user.id === supportAgent.id);

        if (!supportConversation) {
            const newSupportConversationId = `support-${this.supportConversations.length + 1}`;
            supportConversation = {
                id: newSupportConversationId,
                user: supportAgent,
                messages: [
                    {
                        id: Date.now(),
                        text: `您好，很高兴为您服务。请问有什么可以帮您？`,
                        sender: 'user',
                        timestamp: new Date().toISOString(),
                        read: false,
                    }
                ],
                unreadCount: 1,
            };
            this.supportConversations.push(supportConversation);
        }
        return supportConversation.id;
    },

    delistProduct(productId) {
        const product = this.myProducts.find(p => p.id === productId);
        if (product) {
            product.status = 'delisted';
        }
    },

    deleteProduct(productId, isDraft = false) {
        if (isDraft) {
            const index = this.drafts.findIndex(p => p.id === productId);
            if (index !== -1) {
                this.drafts.splice(index, 1);
            }
        } else {
            const index = this.myProducts.findIndex(p => p.id === productId);
            if (index !== -1) {
                this.myProducts.splice(index, 1);
            }
        }
    },

    saveDraft(draftData) {
        const newDraft = {
            id: Date.now(),
            ...draftData,
            imageUrl: draftData.images[0] || '',
        };
        this.drafts.push(newDraft);
    },

    publishProduct(productData) {
        const newProduct = {
            id: Date.now(),
            ...productData,
            imageUrl: productData.images[0] || '',
            status: 'published',
        };
        this.myProducts.push(newProduct);
    },

    deleteFavoriteProducts(productIds) {
        this.favoriteProducts = this.favoriteProducts.filter(p => !productIds.includes(p.id));
    },

    clearInvalidFavorites() {
        this.favoriteProducts = this.favoriteProducts.filter(p => p.status !== 2);
    },

    addCommentToProduct(productId, commentText) {
        const product = this.allProducts.find(p => p.id === productId);
        if (product) {
            if (!product.comments) {
                product.comments = [];
            }
            product.comments.push({
                id: Date.now(),
                user: { name: this.profile.nickname, avatarUrl: this.profile.avatarUrl || 'https://i.pravatar.cc/40?u=me' },
                comment: commentText,
                replies: [],
            });
        }
    },

    addReplyToComment(productId, commentId, replyText) {
        const product = this.allProducts.find(p => p.id === productId);
        if (product && product.comments) {
            const comment = product.comments.find(c => c.id === commentId);
            if (comment) {
                if (!comment.replies) {
                    comment.replies = [];
                }
                comment.replies.push({
                    id: Date.now(),
                    user: { name: this.profile.nickname, avatarUrl: this.profile.avatarUrl || 'https://i.pravatar.cc/40?u=me' },
                    comment: replyText,
                });
            }
        }
    },

    deleteConversations(conversationIds) {
        this.conversations = this.conversations.filter(c => !conversationIds.includes(c.id));
        this.supportConversations = this.supportConversations.filter(c => !conversationIds.includes(c.id));
    },

    addAdditionalReview(orderId, comment, userType) {
        const order = [...this.boughtOrders, ...this.soldOrders].find(o => o.id === orderId);
        if (!order) return;

        const reviewTarget = userType === 'buyer' ? 'buyerReview' : 'sellerReview';

        if (order[reviewTarget] && !order[reviewTarget].additional) {
            order[reviewTarget].additional = {
                comment,
                time: new Date().toLocaleString('zh-CN'),
            };
        }
    },

    requestDispute(orderId) {
        const order = [...this.boughtOrders, ...this.soldOrders].find(o => o.id === orderId);
        if (order && order.refundInfo) {
            order.refundInfo.disputeInProgress = true;

            const lastRejection = order.refundInfo.history.slice().reverse().find(h => h.isRejection);
            if (lastRejection) {
                delete lastRejection.rejectionTimestamp;
            }

            order.refundInfo.history.push({
                time: new Date().toLocaleString('zh-CN'),
                text: '您已发起维权，等待客服介入。',
            });

            const supportAgent = availableSupportAgents[Math.floor(Math.random() * availableSupportAgents.length)];
            let supportConversation = this.supportConversations.find(c => c.user.id === supportAgent.id);

            if (!supportConversation) {
                const newSupportConversationId = `support-${this.supportConversations.length + 1}`;
                supportConversation = {
                    id: newSupportConversationId,
                    user: supportAgent,
                    messages: [
                        {
                            id: Date.now(),
                            text: `您好，关于订单 ${order.orderNumber} 的维权请求，我将为您处理。请描述您的问题。`,
                            sender: 'user',
                            timestamp: new Date().toISOString(),
                            read: false,
                        }
                    ],
                    unreadCount: 1,
                };
                this.supportConversations.push(supportConversation);
            }
            return supportConversation.id;
        }
        return null;
    },

    addMessage(conversationId, messageText) {
        const conversation = this.allConversations.find(c => c.id === conversationId);
        if (conversation) {
            const newMessage = {
                id: Date.now(),
                text: messageText,
                sender: 'me',
                timestamp: new Date().toISOString(),
                read: false,
            };
            conversation.messages.push(newMessage);
        }
    },

    createOrder(product, status = '待付款', shippingInfo) {
        const newOrderId = `B${Date.now()}`;
        const now = new Date().toLocaleString('zh-CN');
        const finalShippingInfo = shippingInfo || this.profile.shippingInfo;
        const newOrder = {
            id: newOrderId,
            seller: product.seller,
            buyer: { name: this.profile.nickname },
            status: status,
            totalPrice: product.price,
            product: {
                id: product.id,
                title: product.title,
                imageUrl: product.images[0].src,
                price: product.price,
            },
            shippingAddress: `${finalShippingInfo.name}, ${finalShippingInfo.phone}, ${finalShippingInfo.address}`,
            orderNumber: String(Date.now()),
            createTime: now,
            payTime: status === '待发货' ? now : null,
            shipTime: null,
            completeTime: null,
            priceDetails: { productTotal: product.price, shippingFee: '0.00' },
        };
        this.boughtOrders.unshift(newOrder);
        return newOrderId;
    },
  },
});
