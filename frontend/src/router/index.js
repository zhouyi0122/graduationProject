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
        // 如果访问的是需要认证的页面，但用户未登录，则重定向到登录页
        return next('/login');
    } 

    next();
});

export default router;


