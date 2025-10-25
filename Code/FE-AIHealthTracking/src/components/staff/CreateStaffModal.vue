<script setup>
import { ref, reactive, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Calendar from 'primevue/calendar';
import Password from 'primevue/password';
import Button from 'primevue/button';

const props = defineProps({
    modelValue: { type: Boolean, default: false }, // v-model:visible
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
    defaultPasswordHint: { type: String, default: 'vcnkids' }
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
    avatar: '', // dataURL
    name: '',
    dob: null,
    gender: null,
    phone: '',
    email: '',
    address: '',
    role: null, // {label, value}
    username: '',
    password: '',
    confirm: ''
});

const errors = reactive({
    name: '',
    phone: '',
    password: '',
    confirm: ''
});

function resetForm() {
    form.avatar = '';
    form.name = '';
    form.dob = null;
    form.gender = null;
    form.phone = '';
    form.email = '';
    form.address = '';
    form.role = null;
    form.username = '';
    form.password = '';
    form.confirm = '';
    Object.keys(errors).forEach((k) => (errors[k] = ''));
}
watch(
    () => visible.value,
    (v) => {
        if (v) resetForm();
    }
);

function validate() {
    let ok = true;
    errors.name = form.name.trim() ? '' : 'Họ tên là bắt buộc';
    errors.phone = form.phone.trim() ? '' : 'Số điện thoại là bắt buộc';
    if (!errors.name && !errors.phone) ok = true;

    // Nếu nhập mật khẩu thì phải khớp confirm
    if (form.password || form.confirm) {
        errors.password = form.password.length >= 6 ? '' : 'Mật khẩu tối thiểu 6 ký tự';
        errors.confirm = form.password === form.confirm ? '' : 'Mật khẩu nhập lại không khớp';
        if (errors.password || errors.confirm) ok = false;
    }
    if (errors.name || errors.phone) ok = false;
    return ok;
}

function toInitials(name) {
    const p = name.trim().split(/\s+/);
    return (p[0]?.[0] || 'N').toUpperCase();
}

function onPickAvatar(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
        form.avatar = reader.result;
    };
    reader.readAsDataURL(file);
}

function onSubmit() {
    if (!validate()) return;

    // Tạo object mock để trả về parent – parent sẽ push vào bảng
    const newStaff = {
        id: Date.now(), // mock id
        name: form.name.trim(),
        email: form.email.trim(),
        username: (form.username || form.email || form.name).trim().replace(/\s+/g, '').toLowerCase(),
        phone: form.phone.trim(),
        classes: [],
        role: form.role?.value || '',
        status: 'active',
        meta: {
            dob: form.dob ? new Date(form.dob).toISOString().slice(0, 10) : null,
            gender: form.gender?.value || null,
            address: form.address.trim(),
            avatar: form.avatar // dataURL – bạn có thể upload lên BE sau
        }
    };

    emit('created', newStaff);
    visible.value = false;
}
</script>

<template>
    <Dialog v-model:visible="visible" modal :draggable="false" class="create-staff-dialog" :breakpoints="{ '960px': '90vw', '640px': '96vw' }" :style="{ width: '560px' }">
        <template #header>
            <div class="flex items-center justify-between w-full">
                <div class="text-lg font-semibold text-slate-800">Tạo nhân viên</div>
            </div>
        </template>

        <!-- Avatar -->
        <div class="flex justify-center mb-4">
            <div class="relative">
                <div class="w-28 h-28 rounded-full bg-blue-100 text-blue-700 flex items-center justify-center overflow-hidden text-3xl select-none">
                    <template v-if="form.avatar">
                        <img :src="form.avatar" alt="avatar" class="w-full h-full object-cover" />
                    </template>
                    <template v-else>
                        {{ toInitials(form.name) }}
                    </template>
                </div>
                <label class="absolute -right-1 top-0 inline-flex items-center justify-center w-7 h-7 rounded-full bg-white border border-slate-200 shadow cursor-pointer hover:bg-slate-50" title="Tải ảnh">
                    <i class="fa-solid fa-pen text-slate-700 text-xs"></i>
                    <input type="file" accept="image/*" class="hidden" @change="onPickAvatar" />
                </label>
            </div>
        </div>

        <!-- Thông tin nhân viên -->
        <div class="section-title">Thông tin nhân viên</div>

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

            <div>
                <label class="label">Số điện thoại <span class="req">*</span></label>
                <InputText v-model="form.phone" class="w-full" placeholder="Số điện thoại" :class="{ 'p-invalid': !!errors.phone }" />
                <small v-if="errors.phone" class="error">{{ errors.phone }}</small>
            </div>

            <div>
                <label class="label">Email</label>
                <InputText v-model="form.email" class="w-full" placeholder="Email" />
            </div>

            <div>
                <label class="label">Địa chỉ</label>
                <InputText v-model="form.address" class="w-full" placeholder="Địa chỉ" />
            </div>
        </div>

        <!-- Chức vụ -->
        <div class="section-title mt-5">Chức vụ</div>
        <div class="mb-3">
            <Dropdown v-model="form.role" :options="roles" optionLabel="label" class="w-full" placeholder="Chức vụ" />
        </div>

        <!-- Thông tin tài khoản -->
        <div class="section-title">Thông tin tài khoản</div>

        <div class="space-y-3">
            <div>
                <label class="label">Tài khoản đăng nhập</label>
                <InputText v-model="form.username" class="w-full" placeholder="Tài khoản đăng nhập" />
            </div>

            <div class="text-slate-600 text-sm">
                Mật khẩu mặc định của nhân viên là <span class="font-semibold">{{ defaultPasswordHint }}</span>
            </div>

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

        <template #footer>
            <div class="flex items-center justify-end gap-2 w-full">
                <Button label="Đóng" class="!bg-slate-200 !text-slate-800 !border-0" @click="visible = false" />
                <Button label="Tạo nhân viên" class="!bg-primary !border-0 !text-white" @click="onSubmit" />
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
