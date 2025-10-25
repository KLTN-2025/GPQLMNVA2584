<script setup>
import { useLayout } from '@/layout/composables/layout';
import AppConfigurator from './AppConfigurator.vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';

// ✅ Dùng SweetAlert2 (từ plugin)
import { getCurrentInstance } from 'vue';
const { proxy } = getCurrentInstance();

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();
const router = useRouter();
const auth = useAuthStore();

async function onLogout() {
    // 🟢 Hỏi người dùng có chắc chắn đăng xuất không
    const result = await proxy.$swal.fire({
        title: 'Xác nhận đăng xuất?',
        text: 'Bạn có chắc muốn đăng xuất khỏi hệ thống?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đăng xuất',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6'
    });

    if (!result.isConfirmed) return;

    try {
        await auth.logout();
        proxy.$alertSuccess('Đăng xuất thành công!', 'Hẹn gặp lại bạn 👋');
        router.push({ name: 'login' });
    } catch (error) {
        proxy.$alertError('Đăng xuất thất bại', 'Vui lòng thử lại sau.');
    }
}
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-left">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>

            <router-link to="/" class="layout-topbar-logo">
                <img src="@/assets/logo-vcnkids.jpg" alt="Logo" class="logo-img" />
                <span>AI Health Tracking</span>
            </router-link>
        </div>

        <div class="layout-topbar-right">
            <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
            </button>

            <div class="layout-topbar-menu">
                <router-link to="/profile" class="layout-topbar-action" role="button"> <i class="pi pi-user"></i><span>Hồ sơ</span> </router-link>

                <router-link to="/settings" class="layout-topbar-action" role="button"> <i class="pi pi-cog"></i><span>Cài đặt</span> </router-link>

                <router-link to="/auth/change-password" class="layout-topbar-action" role="button"> <i class="pi pi-key"></i><span>Đổi mật khẩu</span> </router-link>

                <router-link to="/notifications" class="layout-topbar-action" role="button"> <i class="pi pi-bell"></i><span>Thông báo</span> </router-link>

                <!-- ✅ Nút đăng xuất có xác nhận -->
                <button type="button" class="layout-topbar-action" @click="onLogout"><i class="pi pi-sign-out"></i><span>Đăng xuất</span></button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.layout-topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--surface-card);
    padding: 0 1rem;
    height: 64px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.layout-topbar-left {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.layout-topbar-right {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.layout-topbar-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    text-decoration: none;
    color: inherit;
}

.logo-img {
    height: 40px;
    width: auto;
    object-fit: contain;
}

.layout-topbar-menu {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.layout-topbar-action {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    border-radius: 8px;
    transition: background 0.2s;
}

.layout-topbar-action:hover {
    background: var(--surface-hover, rgba(0, 0, 0, 0.05));
}
</style>
