<script setup>
import { ref, reactive, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Calendar from 'primevue/calendar';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

import { createParent, updateParent } from '@/service/parentService.js';

const props = defineProps({
    modelValue: { type: Boolean, default: false },
    parent: { type: Object, default: null } // nếu có => edit
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
});

const relOptions = [
    { label: 'Cha mẹ học sinh', value: 'Cha mẹ học sinh' },
    { label: 'Single', value: 'Single' },
    { label: 'Khác', value: 'Khác' }
];

const form = reactive({
    id: null,
    fullName: '',
    username: '',
    phone: '',
    email: '',
    dateOfBirth: null,
    occupation: '',
    relationship: '',
    emergencyContact: '',
    additionalPhone: ''
});
const errors = reactive({
    fullName: '',
    phone: '',
    email: ''
});
const submitting = ref(false);
const serverError = ref('');

watch(
    () => visible.value,
    (v) => {
        if (!v) return;
        resetForm();
        if (props.parent) {
            Object.assign(form, {
                id: props.parent.id ?? null,
                fullName: props.parent.name ?? '',
                username: props.parent.username ?? '',
                phone: props.parent.phone ?? '',
                email: props.parent.email ?? '',
                dateOfBirth: props.parent.dob ? new Date(props.parent.dob) : null,
                occupation: props.parent.occupation ?? '',
                relationship: props.parent.relationship ?? '',
                emergencyContact: props.parent.emergencyContact ?? '',
                additionalPhone: props.parent.additionalPhone ?? ''
            });
        }
    }
);

function resetForm() {
    Object.assign(form, {
        id: null,
        fullName: '',
        username: '',
        phone: '',
        email: '',
        dateOfBirth: null,
        occupation: '',
        relationship: '',
        emergencyContact: '',
        additionalPhone: ''
    });
    Object.keys(errors).forEach((k) => (errors[k] = ''));
    serverError.value = '';
    submitting.value = false;
}

function validate() {
    errors.fullName = form.fullName.trim() ? '' : 'Bắt buộc';
    // phone/email có thể optional tuỳ yêu cầu
    return !Object.values(errors).some(Boolean);
}

function toYMD(d) {
    if (!(d instanceof Date) || isNaN(d)) return null;
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

async function onSubmit() {
    if (!validate()) return;
    submitting.value = true;
    serverError.value = '';

    const payload = {
        fullName: form.fullName.trim(),
        username: form.username?.trim() || undefined,
        phone: form.phone?.trim() || undefined,
        email: form.email?.trim() || undefined,
        dateOfBirth: form.dateOfBirth ? toYMD(form.dateOfBirth) : undefined,
        occupation: form.occupation || undefined,
        relationship: form.relationship || undefined,
        emergencyContact: form.emergencyContact || undefined,
        additionalPhone: form.additionalPhone || undefined
    };

    try {
        if (form.id) await updateParent(form.id, payload);
        else await createParent(payload);
        emit('saved');
        visible.value = false;
    } catch (e) {
        serverError.value = e?.response?.data?.message || e?.message || 'Lưu phụ huynh thất bại';
    } finally {
        submitting.value = false;
    }
}
</script>

<template>
    <Dialog v-model:visible="visible" modal :draggable="false" :style="{ width: '720px', maxWidth: '95vw' }">
        <template #header>
            <div class="text-lg font-semibold text-slate-800">{{ form.id ? 'Sửa phụ huynh' : 'Thêm phụ huynh' }}</div>
        </template>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
                <label class="label">Họ và tên <span class="req">*</span></label>
                <InputText v-model="form.fullName" class="w-full" :class="{ 'p-invalid': !!errors.fullName }" />
                <small v-if="errors.fullName" class="error">{{ errors.fullName }}</small>
            </div>
            <div>
                <label class="label">Username</label>
                <InputText v-model="form.username" class="w-full" placeholder="parent_01" />
            </div>
            <div>
                <label class="label">Số điện thoại</label>
                <InputText v-model="form.phone" class="w-full" placeholder="0909xxxxxx" />
            </div>
            <div>
                <label class="label">Email</label>
                <InputText v-model="form.email" class="w-full" placeholder="parent@example.com" />
            </div>
            <div>
                <label class="label">Ngày sinh</label>
                <Calendar v-model="form.dateOfBirth" class="w-full" dateFormat="dd/mm/yy" placeholder="dd/mm/yyyy" />
            </div>
            <div>
                <label class="label">Nghề nghiệp</label>
                <InputText v-model="form.occupation" class="w-full" />
            </div>
            <div>
                <label class="label">Quan hệ</label>
                <Dropdown v-model="form.relationship" :options="relOptions" optionLabel="label" optionValue="value" class="w-full" placeholder="Chọn" />
            </div>
            <div>
                <label class="label">Điện thoại khẩn cấp</label>
                <InputText v-model="form.emergencyContact" class="w-full" placeholder="Nguyễn Văn A - 0912345678" />
            </div>
            <div class="md:col-span-2">
                <label class="label">SĐT bổ sung</label>
                <InputText v-model="form.additionalPhone" class="w-full" placeholder="SĐT 2 (nếu có)" />
            </div>
        </div>

        <div v-if="serverError" class="mt-2 text-sm text-rose-600">{{ serverError }}</div>

        <template #footer>
            <div class="flex items-center justify-end gap-2">
                <Button label="Đóng" class="!bg-slate-200 !text-slate-800 !border-0" :disabled="submitting" @click="visible = false" />
                <Button :label="submitting ? 'Đang lưu...' : form.id ? 'Cập nhật' : 'Tạo mới'" class="!bg-primary !text-white !border-0" :disabled="submitting" @click="onSubmit" />
            </div>
        </template>
    </Dialog>
</template>

<style scoped>
.label {
    display: inline-block;
    margin-bottom: 6px;
    font-weight: 600;
    color: #334155;
}
.req {
    color: #ef4444;
}
.error {
    color: #ef4444;
    font-size: 0.8rem;
}
</style>
