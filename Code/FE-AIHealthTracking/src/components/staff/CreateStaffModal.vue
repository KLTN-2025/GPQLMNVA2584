<script setup>
import { ref, reactive, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Calendar from 'primevue/calendar';
import Password from 'primevue/password';
import Button from 'primevue/button';
import { createOrRegisterTeacher } from '@/service/teacherService.js';

const props = defineProps({
    modelValue: { type: Boolean, default: false },
    roles: {
        type: Array,
        default: () => [
            { label: 'Hiệu trưởng', value: 'Hiệu trưởng' },
            { label: 'Quản lý trường', value: 'Quản lý trường' },
            { label: 'Giáo viên', value: 'Giáo viên' },
            { label: 'Nhân viên', value: 'Nhân viên' },
            { label: 'Nhân viên y tế', value: 'Nhân viên y tế' }
        ]
    },
    defaultPasswordHint: { type: String, default: '123456' }
});
const emit = defineEmits(['update:modelValue', 'created']);

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
});

const genders = [
    { label: 'Nam', value: 'M' },
    { label: 'Nữ', value: 'F' }
];

const form = reactive({
    avatarPreview: '',
    avatarFile: null,
    name: '',
    dob: null,
    gender: null,
    phone: '',
    email: '',
    address: '',
    role: null,
    username: '',
    password: '',
    confirm: '',
    specialization: '',
    employeeCode: '',
    emergencyContact: ''
});

const errors = reactive({ name: '', phone: '', username: '', password: '', confirm: '' });
const serverError = ref('');
const serverWarn = ref('');
const submitLoading = ref(false);

function resetForm() {
    Object.assign(form, {
        avatarPreview: '',
        avatarFile: null,
        name: '',
        dob: null,
        gender: null,
        phone: '',
        email: '',
        address: '',
        role: null,
        username: '',
        password: '',
        confirm: '',
        specialization: '',
        employeeCode: '',
        emergencyContact: ''
    });
    Object.keys(errors).forEach((k) => (errors[k] = ''));
    serverError.value = '';
    serverWarn.value = '';
    submitLoading.value = false;
}
watch(
    () => visible.value,
    (v) => {
        if (v) resetForm();
    }
);

function validate() {
    errors.name = form.name.trim() ? '' : 'Họ tên là bắt buộc';
    errors.phone = form.phone.trim() ? '' : 'Số điện thoại là bắt buộc';
    errors.username = form.username.trim() ? '' : 'Tài khoản là bắt buộc';
    if (form.password || form.confirm) {
        errors.password = form.password.length >= 6 ? '' : 'Mật khẩu tối thiểu 6 ký tự';
        errors.confirm = form.password === form.confirm ? '' : 'Mật khẩu nhập lại không khớp';
    } else {
        errors.password = '';
        errors.confirm = '';
    }
    return !(errors.name || errors.phone || errors.username || errors.password || errors.confirm);
}

function toInitials(name) {
    const p = name.trim().split(/\s+/);
    return (p[0]?.[0] || 'N').toUpperCase();
}

function onPickAvatar(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    form.avatarFile = file;
    const reader = new FileReader();
    reader.onload = () => {
        form.avatarPreview = reader.result;
    };
    reader.readAsDataURL(file);
}

function genEmployeeCodeIfEmpty() {
    if (form.employeeCode?.trim()) return form.employeeCode.trim();
    const y = new Date().getFullYear();
    const rand = Math.floor(10000 + Math.random() * 90000);
    return `GV${y}-${rand}`;
}

function suggestUsernameIfEmpty() {
    if (form.username?.trim()) return form.username.trim();
    return (
        form.name
            .trim()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-zA-Z0-9 ]/g, '')
            .trim()
            .replace(/\s+/g, '.')
            .toLowerCase() || `gv.${Date.now()}`
    );
}

async function onSubmit() {
    serverError.value = '';
    serverWarn.value = '';
    if (!validate()) return;

    const payload = {
        username: suggestUsernameIfEmpty(),
        password: form.password || props.defaultPasswordHint,
        fullName: form.name.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        gender: form.gender?.value,
        specialization: form.specialization,
        employeeCode: genEmployeeCodeIfEmpty(),
        emergencyContact: form.emergencyContact,
        dateOfBirth: form.dob,
        avatarUrl: ''
    };

    submitLoading.value = true;
    try {
        const { teacher, errorDup, warning } = await createOrRegisterTeacher(payload, form.avatarFile);
        if (warning) serverWarn.value = warning; // thông báo đã tự điều chỉnh username/email/employeeCode
        if (errorDup) {
            serverError.value = errorDup;
            return;
        }
        emit('created', teacher);
        visible.value = false;
    } catch (e) {
        serverError.value = e?.response?.data?.message || e?.message || 'Có lỗi xảy ra';
    } finally {
        submitLoading.value = false;
    }
}
</script>

<template>
    <Dialog v-model:visible="visible" modal :draggable="false" class="create-staff-dialog" :breakpoints="{ '960px': '90vw', '640px': '96vw' }" :style="{ width: '560px' }">
        <template #header>
            <div class="flex items-center justify-between w-full">
                <div class="text-lg font-semibold text-slate-800">Tạo giáo viên</div>
            </div>
        </template>

        <div class="flex justify-center mb-4">
            <div class="relative">
                <div class="w-28 h-28 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center overflow-hidden text-3xl select-none">
                    <template v-if="form.avatarPreview"><img :src="form.avatarPreview" alt="avatar" class="w-full h-full object-cover" /></template>
                    <template v-else>{{ toInitials(form.name) }}</template>
                </div>
                <label class="absolute -right-1 top-0 inline-flex items-center justify-center w-7 h-7 rounded-full bg-white border border-slate-200 shadow cursor-pointer hover:bg-slate-50" title="Tải ảnh">
                    <i class="fa-solid fa-pen text-slate-700 text-xs"></i>
                    <input type="file" accept="image/*" class="hidden" @change="onPickAvatar" />
                </label>
            </div>
        </div>

        <div class="section-title">Thông tin giáo viên</div>
        <div class="space-y-3">
            <div>
                <label class="label">Họ tên <span class="req">*</span></label>
                <InputText v-model="form.name" class="w-full" placeholder="Họ tên" :class="{ 'p-invalid': !!errors.name }" />
                <small v-if="errors.name" class="error">{{ errors.name }}</small>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label class="label">Ngày sinh</label>
                    <Calendar v-model="form.dob" class="w-full" dateFormat="dd/mm/yy" placeholder="Ngày sinh" />
                </div>
                <div>
                    <label class="label">Giới tính</label>
                    <Dropdown v-model="form.gender" :options="genders" optionLabel="label" class="w-full" placeholder="Giới tính" />
                </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label class="label">Số điện thoại <span class="req">*</span></label>
                    <InputText v-model="form.phone" class="w-full" placeholder="Số điện thoại" :class="{ 'p-invalid': !!errors.phone }" />
                    <small v-if="errors.phone" class="error">{{ errors.phone }}</small>
                </div>
                <div>
                    <label class="label">Email</label>
                    <InputText v-model="form.email" class="w-full" placeholder="Email" />
                </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label class="label">Chuyên môn</label>
                    <InputText v-model="form.specialization" class="w-full" placeholder="Chuyên môn" />
                </div>
                <div>
                    <label class="label">Mã nhân viên</label>
                    <InputText v-model="form.employeeCode" class="w-full" placeholder="Mã nhân viên (để trống sẽ tự sinh)" />
                </div>
            </div>

            <div>
                <label class="label">Liên hệ khẩn cấp</label>
                <InputText v-model="form.emergencyContact" class="w-full" placeholder="Số điện thoại người thân" />
            </div>
        </div>

        <div class="section-title">Tài khoản</div>
        <div class="space-y-3">
            <div>
                <label class="label">Tài khoản đăng nhập <span class="req">*</span></label>
                <InputText v-model="form.username" class="w-full" placeholder="Tài khoản đăng nhập (để trống sẽ gợi ý từ tên)" :class="{ 'p-invalid': !!errors.username }" />
                <small v-if="errors.username" class="error">{{ errors.username }}</small>
            </div>

            <div class="text-slate-600 text-sm">
                Mật khẩu mặc định <span class="font-semibold">{{ defaultPasswordHint }}</span> (có thể để trống để dùng mặc định)
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                    <label class="label">Mật khẩu</label>
                    <Password v-model="form.password" :feedback="false" :toggleMask="true" class="w-full" inputClass="w-full" placeholder="Mật khẩu" />
                    <small v-if="errors.password" class="error">{{ errors.password }}</small>
                </div>
                <div>
                    <label class="label">Nhập lại mật khẩu</label>
                    <Password v-model="form.confirm" :feedback="false" :toggleMask="true" class="w-full" inputClass="w-full" placeholder="Nhập lại mật khẩu" />
                    <small v-if="errors.confirm" class="error">{{ errors.confirm }}</small>
                </div>
            </div>
        </div>

        <div v-if="serverWarn" class="mt-2 text-xs text-amber-600">
            {{ serverWarn }}
        </div>
        <div v-if="serverError" class="mt-2 text-sm text-rose-600 font-medium">
            {{ serverError }}
        </div>

        <template #footer>
            <div class="flex items-center justify-end gap-2 w-full">
                <Button label="Đóng" class="!bg-slate-200 !text-slate-800 !border-0" :disabled="submitLoading" @click="visible = false" />
                <Button :label="submitLoading ? 'Đang tạo...' : 'Tạo giáo viên'" :disabled="submitLoading" class="!bg-primary !border-0 !text-white" @click="onSubmit" />
            </div>
        </template>
    </Dialog>
</template>

<style scoped>
.section-title {
    margin: 10px 0 8px;
    font-weight: 700;
    color: #0f172a;
}
.label {
    display: inline-block;
    margin-bottom: 6px;
    color: #334155;
    font-weight: 600;
    font-size: 0.925rem;
}
.req {
    color: #ef4444;
}
.error {
    color: #ef4444;
}
:deep(.p-inputtext.p-invalid) {
    border-color: #ef4444;
}
</style>
