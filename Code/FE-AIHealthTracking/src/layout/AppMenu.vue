<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'HOME',
        items: [
            { label: 'Trang Chủ', icon: 'fa-solid fa-house-user', to: '/school/Trang-Chu' },
        ]
    },

    // === QUẢN LÝ CHUNG ===
    {
        label: 'Quản lý chung',
        items: [
            { label: 'Thông tin nhà trường', icon: 'fa-solid fa-building', to: '/school/info' },
            { label: 'Cấu hình', icon: 'fa-solid fa-gear', to: '/school/settings' },
            { label: 'Năm học', icon: 'fa-solid fa-calendar-days', to: '/school/year' },
            { label: 'Cấp quản lý', icon: 'fa-solid fa-layer-group', to: '/school/levels' },
            { label: 'Lớp học', icon: 'fa-solid fa-trophy', to: '/school/class' }
        ]
    },

    // === QUẢN LÝ NGƯỜI DÙNG ===
    {
        label: 'Quản lý người dùng',
        items: [
            { label: 'Giáo Viên', icon: 'fa-solid fa-id-badge', to: '/school/giao-Vien' },
            { label: 'Học Sinh', icon: 'fa-solid fa-graduation-cap', to: '/school/Hoc-Sinh' },
            { label: 'Phụ Huynh', icon: 'fa-solid fa-user-group', to: '/school/parent' },
            { label: 'Nhân Viên', icon: 'fa-solid fa-id-badge', to: '/school/qlNhan-Vien' },
        ]
    },

    // === HOẠT ĐỘNG ===
    {
        label: 'Hoạt động',
        items: [
            { label: 'Thực đơn', icon: 'fa-solid fa-clipboard-list', to: '/activity/menu' },
            { label: 'Điểm danh', icon: 'fa-solid fa-check-circle', to: '/activity/attendance' },
            { label: 'Đơn xin nghỉ', icon: 'fa-solid fa-file-lines', to: '/activity/leave-request' },
            { label: 'Đơn xin bảo lưu', icon: 'fa-solid fa-file-medical', to: '/activity/suspend' },
            { label: 'Y tế', icon: 'fa-solid fa-notes-medical', to: '/activity/health' },
            { label: 'Album', icon: 'fa-solid fa-image', to: '/activity/album' },
            { label: 'Bài viết', icon: 'fa-solid fa-pen-to-square', to: '/activity/posts' },
            { label: 'Hòm thư góp ý', icon: 'fa-solid fa-envelope', to: '/activity/feedback' }
        ]
    },

    // === TÀI CHÍNH ===
    {
        label: 'Tài chính',
        items: [
            { label: 'Thiết lập', icon: 'fa-solid fa-gear', to: '/finance/settings' },
            { label: 'Thông tin nhập học', icon: 'fa-solid fa-user-check', to: '/finance/admissions' },
            { label: 'Đợt thu', icon: 'fa-solid fa-money-bill', to: '/finance/collect' }
        ]
    },

    // === BÁO CÁO & THỐNG KÊ ===
    {
        label: 'Báo cáo & Thống kê',
        items: [
            { label: 'Báo cáo điểm danh', icon: 'fa-solid fa-file-lines', to: '/reports/attendance' },
            { label: 'Báo cáo trông muộn', icon: 'fa-solid fa-file-circle-exclamation', to: '/reports/late' },
            { label: 'Báo cáo thôi học', icon: 'fa-solid fa-file-circle-minus', to: '/reports/quit' },
            { label: 'Báo cáo bảo lưu', icon: 'fa-solid fa-file-circle-plus', to: '/reports/suspend' },
            { label: 'Báo cáo khác', icon: 'fa-solid fa-file-circle-question', to: '/reports/others' },
            { label: 'Nhật ký hoạt động', icon: 'fa-solid fa-list', to: '/reports/logs' }
        ]
    }

    //    {
    //     label: 'UI Components',
    //     items: [
    //         { label: 'Form Layout', icon: 'pi pi-fw pi-id-card', to: '/uikit/formlayout' },
    //         { label: 'Input', icon: 'pi pi-fw pi-check-square', to: '/uikit/input' },
    //         { label: 'Button', icon: 'pi pi-fw pi-mobile', to: '/uikit/button', class: 'rotated-icon' },
    //         { label: 'Table', icon: 'pi pi-fw pi-table', to: '/uikit/table' },
    //         { label: 'List', icon: 'pi pi-fw pi-list', to: '/uikit/list' },
    //         { label: 'Tree', icon: 'pi pi-fw pi-share-alt', to: '/uikit/tree' },
    //         { label: 'Panel', icon: 'pi pi-fw pi-tablet', to: '/uikit/panel' },
    //         { label: 'Overlay', icon: 'pi pi-fw pi-clone', to: '/uikit/overlay' },
    //         { label: 'Media', icon: 'pi pi-fw pi-image', to: '/uikit/media' },
    //         { label: 'Menu', icon: 'pi pi-fw pi-bars', to: '/uikit/menu' },
    //         { label: 'Message', icon: 'pi pi-fw pi-comment', to: '/uikit/message' },
    //         { label: 'File', icon: 'pi pi-fw pi-file', to: '/uikit/file' },
    //         { label: 'Chart', icon: 'pi pi-fw pi-chart-bar', to: '/uikit/charts' },
    //         { label: 'Timeline', icon: 'pi pi-fw pi-calendar', to: '/uikit/timeline' },
    //         { label: 'Misc', icon: 'pi pi-fw pi-circle', to: '/uikit/misc' }
    //     ]
    // },
    // {
    //     label: 'Pages',
    //     icon: 'pi pi-fw pi-briefcase',
    //     to: '/pages',
    //     items: [
    //         {
    //             label: 'Landing',
    //             icon: 'pi pi-fw pi-globe',
    //             to: '/landing'
    //         },
    //         {
    //             label: 'Auth',
    //             icon: 'pi pi-fw pi-user',
    //             items: [
    //                 {
    //                     label: 'Login',
    //                     icon: 'pi pi-fw pi-sign-in',
    //                     to: '/auth/login'
    //                 },
    //                 {
    //                     label: 'Error',
    //                     icon: 'pi pi-fw pi-times-circle',
    //                     to: '/auth/error'
    //                 },
    //                 {
    //                     label: 'Access Denied',
    //                     icon: 'pi pi-fw pi-lock',
    //                     to: '/auth/access'
    //                 }
    //             ]
    //         },
    //         {
    //             label: 'Crud',
    //             icon: 'pi pi-fw pi-pencil',
    //             to: '/pages/crud'
    //         },
    //         {
    //             label: 'Not Found',
    //             icon: 'pi pi-fw pi-exclamation-circle',
    //             to: '/pages/notfound'
    //         },
    //         {
    //             label: 'Empty',
    //             icon: 'pi pi-fw pi-circle-off',
    //             to: '/pages/empty'
    //         }
    //     ]
    // },
    // {
    //     label: 'Hierarchy',
    //     items: [
    //         {
    //             label: 'Submenu 1',
    //             icon: 'pi pi-fw pi-bookmark',
    //             items: [
    //                 {
    //                     label: 'Submenu 1.1',
    //                     icon: 'pi pi-fw pi-bookmark',
    //                     items: [
    //                         { label: 'Submenu 1.1.1', icon: 'pi pi-fw pi-bookmark' },
    //                         { label: 'Submenu 1.1.2', icon: 'pi pi-fw pi-bookmark' },
    //                         { label: 'Submenu 1.1.3', icon: 'pi pi-fw pi-bookmark' }
    //                     ]
    //                 },
    //                 {
    //                     label: 'Submenu 1.2',
    //                     icon: 'pi pi-fw pi-bookmark',
    //                     items: [{ label: 'Submenu 1.2.1', icon: 'pi pi-fw pi-bookmark' }]
    //                 }
    //             ]
    //         },
    //         {
    //             label: 'Submenu 2',
    //             icon: 'pi pi-fw pi-bookmark',
    //             items: [
    //                 {
    //                     label: 'Submenu 2.1',
    //                     icon: 'pi pi-fw pi-bookmark',
    //                     items: [
    //                         { label: 'Submenu 2.1.1', icon: 'pi pi-fw pi-bookmark' },
    //                         { label: 'Submenu 2.1.2', icon: 'pi pi-fw pi-bookmark' }
    //                     ]
    //                 },
    //                 {
    //                     label: 'Submenu 2.2',
    //                     icon: 'pi pi-fw pi-bookmark',
    //                     items: [{ label: 'Submenu 2.2.1', icon: 'pi pi-fw pi-bookmark' }]
    //                 }
    //             ]
    //         }
    //     ]
    // },
    // {
    //     label: 'Get Started',
    //     items: [
    //         {
    //             label: 'Documentation',
    //             icon: 'pi pi-fw pi-book',
    //             to: '/documentation'
    //         },
    //         {
    //             label: 'View Source',
    //             icon: 'pi pi-fw pi-github',
    //             url: 'https://github.com/primefaces/sakai-vue',
    //             target: '_blank'
    //         }
    //     ]
    // }
]);
</script>

<template>
     <ul class="layout-menu">
        <template v-for="(group, i) in model" :key="i">
            <li class="layout-menu-group">{{ group.label }}</li>
            <li v-for="(item, j) in group.items" :key="j" class="layout-menu-item">
                <router-link :to="item.to" class="menu-link flex items-center gap-2">
                    <i :class="item.icon"></i>
                    <span>{{ item.label }}</span>
                    <i class="fa-solid fa-angle-right ml-auto text-gray-400"></i>
                </router-link>
            </li>
        </template>
    </ul>
</template>

<style scoped>
.layout-menu {
    list-style: none;
    padding: 0;
    margin: 0;
}

.layout-menu-group {
    font-weight: 700;
    font-size: 0.9rem;
    color: #374151;
    text-transform: uppercase;
    margin-top: 1.2rem;
    padding: 0.75rem 1rem;
    border-top: 1px solid #e5e7eb;
}

.layout-menu-item {
    padding: 0.4rem 1rem;
}

.menu-link {
    display: flex;
    align-items: center;
    color: #374151;
    text-decoration: none;
    transition: all 0.2s;
}

.menu-link:hover {
    background-color: #f3f4f6;
    border-radius: 0.5rem;
}

.menu-link i:first-child {
    width: 1.25rem;
    text-align: center;
}

.menu-link .fa-angle-right {
    opacity: 0;
    transition: opacity 0.2s;
}

.menu-link:hover .fa-angle-right {
    opacity: 1;
}

/* Làm nổi bật HOME */
.layout-menu-group:first-child {
    border-top: none;
    color: #2563eb;
    font-weight: 800;
}
</style>
