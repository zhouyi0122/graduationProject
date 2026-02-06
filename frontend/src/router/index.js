import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue') // 懒加载
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue') // 懒加载
    },
    {
        path: '/products/new',
        name: 'ProductCreate',
        component: () => import('../views/ProductCreate.vue')
    },
    {
        path: '/product/edit/:id',
        name: 'ProductEdit',
        component: () => import('../views/ProductCreate.vue')
    },
    {
        path: '/order/confirm',
        name: 'OrderConfirmation',
        component: () => import('../views/OrderConfirmation.vue')
    },
    {
        path: '/product/:id',
        name: 'ProductDetail',
        component: () => import('../views/ProductDetail.vue')
    },
    {
        path: '/products',
        name: 'ProductList',
        component: () => import('../views/ProductList.vue')
    },
    {
        path: '/user',
        name: 'UserPage',
        component: () => import('../views/UserPage.vue')
    },
    {
        path: '/user/products',
        name: 'MyProducts',
        component: () => import('../views/user/MyProducts.vue')
    },
    {
        path: '/user/favorites',
        name: 'MyFavorites',
        component: () => import('../views/user/MyFavorites.vue')
    },
    {
        path: '/user/orders',
        name: 'MyOrders',
        component: () => import('../views/user/MyOrders.vue')
    },
    {
        path: '/user/order/:id',
        name: 'OrderDetail',
        component: () => import('../views/user/OrderDetail.vue')
    },
    {
        path: '/user/order/:id/review',
        name: 'OrderReview',
        component: () => import('../views/user/OrderReview.vue')
    },
    {
        path: '/user/profile',
        name: 'Profile',
        component: () => import('../views/user/Profile.vue')
    },
    {
        path: '/user/account',
        name: 'Account',
        component: () => import('../views/user/Account.vue')
    },
    {
        path: '/chat',
        name: 'Chat',
        component: () => import('../layouts/ChatLayout.vue')
    },
    {
        path: '/chat/:id',
        name: 'ChatWindow',
        component: () => import('../views/chat/ChatWindow.vue')
    },
    {
        path: '/chat/system/:type',
        name: 'SystemNotification',
        component: () => import('../views/chat/SystemNotification.vue')
    },
    {
        path: '/chat/support',
        name: 'SupportList',
        component: () => import('../views/chat/SupportList.vue')
    },
    {
        path: '/admin',
        component: () => import('../layouts/AdminLayout.vue'),
        meta: { requiresAdmin: true },
        children: [
            {
                path: '',
                name: 'Admin',
                redirect: '/admin/dashboard'
            },
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/admin/Dashboard.vue')
            },
            {
                path: 'users',
                name: 'UserManagement',
                component: () => import('../views/admin/UserManagement.vue')
            },
            {
                path: 'products',
                name: 'ProductManagement',
                component: () => import('../views/admin/ProductManagement.vue')
            },

            {
                path: 'orders',
                name: 'OrderManagement',
                component: () => import('../views/admin/OrderManagement.vue')
            },
            {
                path: 'orders/:id',
                name: 'AdminOrderDetail',
                component: () => import('../views/user/OrderDetail.vue')
            },
            {
                path: 'chat',
                name: 'AdminChat',
                component: () => import('../views/admin/AdminChat.vue')
            }
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
    const publicPages = ['/login', '/register'];
    const authRequired = !publicPages.includes(to.path);
    const auth = useAuthStore();

    if (authRequired && !auth.isLoggedIn) {
        return next('/login');
    }

    if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (!auth.isAdmin) {
            return next('/'); // Redirect non-admins to home page
        }
    }

    next();
});

export default router;
