import { useAuthStore } from '@/stores/auth.js';

export function attachAuthGuard(router, pinia) {
    router.beforeEach((to, from, next) => {
        const auth = useAuthStore(pinia);
        const isAuth = !!auth.accessToken;

        // Debug: bật khi cần
        // console.log('[Guard]', { to: to.fullPath, from: from.fullPath, isAuth });

        if (to.matched.some((r) => r.meta?.requiresAuth) && !isAuth) {
            return next({ name: 'login', query: { redirect: to.fullPath } });
        }
        if (to.matched.some((r) => r.meta?.guestOnly) && isAuth) {
            return next({ name: 'dashboard' });
        }
        next();
    });
}
