<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import loginBg from '@/assets/abc.jpg';
import { useAuthStore } from '@/stores/auth.js';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const username = ref('');
const password = ref('');
const checked = ref(false);
const loading = ref(false);
const errorMsg = ref('');

async function onSubmit() {
  errorMsg.value = '';
  if (!username.value || !password.value) {
    errorMsg.value = 'Vui lòng nhập tài khoản và mật khẩu';
    return;
  }
  loading.value = true;
  try {
    const data = await auth.login({ username: username.value, password: password.value });

    // Debug khi cần
    // console.log('BE accessToken:', data?.accessToken || data?.access_token || data?.token);
    // console.log('Store accessToken:', auth.accessToken);

    await nextTick(); // đảm bảo state đã cập nhật

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
    await router.replace(redirect || '/');
  } catch (err) {
    errorMsg.value = err?.response?.data?.message || 'Đăng nhập thất bại, vui lòng kiểm tra lại.';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
    <FloatingConfigurator />

    <div class="relative min-h-screen w-screen overflow-hidden bg-cover bg-center bg-no-repeat" :style="{ backgroundImage: `url(${loginBg})` }">
        <div class="absolute inset-0 bg-gradient-to-b from-black/30 via-black/20 to-black/40"></div>

        <div class="relative z-10 container mx-auto px-4 py-10">
            <div class="mx-auto max-w-md">
                <div class="p-[1px] rounded-3xl bg-gradient-to-b from-primary/70 to-transparent shadow-2xl">
                    <div class="rounded-3xl border border-white/20 dark:border-white/10 bg-white/70 dark:bg-surface-900/60 backdrop-blur-xl px-8 py-10">
                        <div class="text-center mb-8">
                            <img src="@/assets/logo-vcnkids.jpg" alt="Logo" class="w-14 h-14 rounded-xl shadow-lg mx-auto mb-4" />
                            <h1 class="text-surface-900 dark:text-surface-0 text-3xl font-semibold">Health Tracking Kids</h1>
                            <p class="text-muted-color mt-2">Sign in to continue</p>
                        </div>

                        <form @submit.prevent="onSubmit">
                            <InputGroup class="mb-6">
                                <InputGroupAddon><i class="pi pi-user"></i></InputGroupAddon>
                                <InputText v-model="username" placeholder="Tên đăng nhập" autocomplete="username" />
                            </InputGroup>

                            <InputGroup class="mb-2">
                                <InputGroupAddon><i class="pi pi-lock"></i></InputGroupAddon>
                                <Password v-model="password" :feedback="false" :toggleMask="true" placeholder="Nhập mật khẩu" autocomplete="current-password" />
                            </InputGroup>

                            <div v-if="errorMsg" class="text-red-600 text-sm mb-4">{{ errorMsg }}</div>

                            <div class="flex items-center justify-between mt-3 mb-8 gap-6">
                                <div class="flex items-center">
                                    <Checkbox v-model="checked" id="rememberme1" binary class="mr-2" />
                                    <label for="rememberme1" class="text-surface-700 dark:text-surface-200"> Remember me </label>
                                </div>
                                <button type="button" class="font-medium text-primary hover:opacity-90 transition-opacity">Forgot password?</button>
                            </div>

                            <Button
                                type="submit"
                                :loading="loading"
                                label="Đăng Nhập"
                                class="w-full h-12 rounded-xl border-0 shadow-lg bg-gradient-to-r from-primary to-primary-400 hover:from-primary-500 hover:to-primary-400 text-white focus:ring-2 focus:ring-offset-2 focus:ring-primary/40"
                            />
                        </form>

                        <div class="flex items-center my-8">
                            <div class="h-px flex-1 bg-surface-200 dark:bg-surface-700"></div>
                            <div class="h-px flex-1 bg-surface-200 dark:bg-surface-700"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pi-eye,
.pi-eye-slash {
    transform: scale(1.25);
}
</style>
