import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';

import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';
import StyleClass from 'primevue/styleclass'; // THÊM DÒNG NÀY

import '@fortawesome/fontawesome-free/css/all.min.css';
import 'primeicons/primeicons.css';
import '@/assets/styles.scss';

import { useAuthStore } from '@/stores/auth.js';
import { setAuthStore } from '@/stores/storeRegistry.js';
import { attachAuthGuard } from '@/router/guard.js';
import SweetAlertPlugin from './plugins/sweetalert';
const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

// Auth + guard
const authStore = useAuthStore(pinia);
authStore.initFromStorage();
setAuthStore(authStore);
attachAuthGuard(router, pinia);

// PrimeVue
app.use(PrimeVue, {
  theme: { preset: Aura, options: { darkModeSelector: '.app-dark' } }
});
app.use(ToastService);
app.use(ConfirmationService);

// ĐĂNG KÝ DIRECTIVE CHO v-styleclass
app.directive('styleclass', StyleClass);
app.use(SweetAlertPlugin);
app.use(router);
app.mount('#app');
