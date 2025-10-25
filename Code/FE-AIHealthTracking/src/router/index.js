import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            meta: { requiresAuth: true }, // tất cả route con yêu cầu đăng nhập
            children: [
                {
                    path: '', // KHÔNG dùng '/', để tương đối -> kế thừa meta
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue')
                },
                {
                    path: 'school/Trang-Chu', // KHÔNG có '/' đầu
                    name: 'TrangChu',
                    component: () => import('@/views/school/Trang-Chu.vue')
                },
                {
                    path: 'school/qlNhan-Vien',
                    name: 'QlNhanVien',
                    component: () => import('@/views/school/QlNhan-Vien.vue')
                },
                {
                    path: 'school/Hoc-Sinh',
                    name: 'QLHocSinh',
                    component: () => import('@/views/school/Hoc-Sinh.vue')
                }
            ]
        },

        // Public routes
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue')
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue')
        },
        {
            path: '/auth/login',
            name: 'login',
            component: () => import('@/views/pages/auth/Login.vue'),
            meta: { guestOnly: true }
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue')
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue')
        },

        // Fallback 404
        { path: '/:pathMatch(.*)*', redirect: { name: 'notfound' } }
    ]
});

export default router;
