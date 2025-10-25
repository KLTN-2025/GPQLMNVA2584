<script setup lang="ts">
import { ref, computed } from 'vue';
import '@fortawesome/fontawesome-free/css/all.min.css';
import Chart from 'primevue/chart';
import Dropdown from 'primevue/dropdown';

/* ========== Mock data gốc của bạn ========== */
type StatItem = {
  key: 'classes' | 'students' | 'teachers' | 'staff' | 'parents';
  label: string;
  value: number;
  icon: string;
  bg: string;
  fg: string;
};
type ReviewItem = { label: string; value: number; accent?: string; link?: string };

const schoolName = ref('Mầm non Canada Maple Bear');
const academicYear = ref('2024 - 2025');

const stats = ref<StatItem[]>([
  { key: 'classes',  label: 'Tổng số lớp',       value: 19,  icon: 'fa-solid fa-school',            bg: 'bg-blue-100',     fg: 'text-blue-600' },
  { key: 'students', label: 'Tổng số học sinh',  value: 433, icon: 'fa-solid fa-children',          bg: 'bg-emerald-100',  fg: 'text-emerald-600' },
  { key: 'teachers', label: 'Tổng số giáo viên', value: 64,  icon: 'fa-solid fa-person-chalkboard', bg: 'bg-amber-100',   fg: 'text-amber-600' },
  { key: 'staff',    label: 'Tổng số nhân viên', value: 17,  icon: 'fa-solid fa-user-tie',          bg: 'bg-fuchsia-100', fg: 'text-fuchsia-600' },
  { key: 'parents',  label: 'Tổng số phụ huynh', value: 411, icon: 'fa-solid fa-people-group',      bg: 'bg-cyan-100',    fg: 'text-cyan-600' }
]);

const review = ref<ReviewItem[]>([
  { label: 'Đơn xin nghỉ chưa duyệt', value: 232, accent: 'text-red-500', link: '#' },
  { label: 'Đơn bảo lưu chưa duyệt', value: 9, accent: 'text-orange-500', link: '#' },
  { label: 'Góp ý chưa xử lý', value: 47, accent: 'text-yellow-600', link: '#' },
  { label: 'Bài viết chưa kiểm duyệt', value: 27, accent: 'text-sky-600', link: '#' },
  { label: 'Album chưa kiểm duyệt', value: 6, accent: 'text-teal-600', link: '#' }
]);


/* ========== Helpers chung ========== */
const studentsCount = computed(() => stats.value.find(s => s.key === 'students')?.value ?? 0);

const todayStr = computed(() => {
  const d = new Date();
  const weekdays = ['Chủ nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
  return `${weekdays[d.getDay()]}, ${d.toLocaleDateString('vi-VN')}`;
});

/* ========== 1) Donut: 433 Học sinh (trạng thái) ========== */
const studentStatusLabels = [
  `Đang đi học (${studentsCount.value})`,
  'Chờ phân lớp (2)',
  'Đã bảo lưu (0)',
  'Đã thôi học (45)',
  'Đã tốt nghiệp (82)'
];
const studentStatusData = ref({
  labels: studentStatusLabels,
  datasets: [
    {
      data: [studentsCount.value, 2, 0, 45, 82],
      backgroundColor: ['#4C6EF5', '#8CE0FF', '#FFC857', '#EF4444', '#5AC8FA'],
      hoverBackgroundColor: ['#3B5BDB', '#74C7EC', '#FFB020', '#DC2626', '#38BDF8'],
      borderWidth: 0
    }
  ]
});
const doughnutOptionsRightLegend = {
  cutout: '60%',
  plugins: {
    legend: {
      position: 'right',
      labels: { usePointStyle: true, boxWidth: 10, padding: 16 }
    },
    tooltip: {
      callbacks: {
        label: (ctx: any) => `${ctx.label?.split(' (')[0]}: ${ctx.parsed}`
      }
    }
  },
  maintainAspectRatio: false,
  responsive: true
};

/* ========== 2) Donut: Điểm danh 433 học sinh ========== */
const attendanceLabels = ['Đi học (0)', 'Có phép (0)', 'Không phép (0)', 'Đột xuất (0)', 'Chưa xác định (0)', 'Đã về (0)', 'Bảo lưu (0)'];
const attendanceData = ref({
  labels: attendanceLabels,
  datasets: [
    {
      data: [0, 0, 0, 0, 0, 0, 0],
      backgroundColor: ['#4C6EF5','#22C55E','#F59E0B','#EF4444','#38BDF8','#16A34A','#FB7185'],
      hoverBackgroundColor: ['#3B5BDB','#16A34A','#D97706','#DC2626','#0EA5E9','#15803D','#F43F5E'],
      borderWidth: 0
    }
  ]
});

/* ========== 3) Bài viết tuần (bar + line) & Album tuần (line) ========== */
function lastNDaysLabels(n = 7) {
  const labels: string[] = [];
  const d = new Date();
  for (let i = n - 1; i >= 0; i--) {
    const di = new Date(d);
    di.setDate(d.getDate() - i);
    const dd = di.getDate().toString().padStart(2, '0');
    const mm = (di.getMonth() + 1).toString().padStart(2, '0');
    labels.push(`${dd}/${mm}`);
  }
  return labels;
}
const weekLabels = ref(lastNDaysLabels(7));

const postsWeekData = ref({
  labels: weekLabels.value,
  datasets: [
    { type: 'bar', label: 'Bài viết', data: [0, 5, 0, 0, 0, 0, 0], backgroundColor: '#22D3EE', borderRadius: 6, barThickness: 24 },
    { type: 'line', label: 'Like', data: [0, 0, 0, 0, 0, 0, 0], borderColor: '#3B5BDB', backgroundColor: '#3B5BDB', tension: 0.3, pointRadius: 3 },
    { type: 'line', label: 'Comment', data: [0, 0, 0, 0, 0, 0, 0], borderColor: '#22C55E', backgroundColor: '#22C55E', tension: 0.3, pointRadius: 3 }
  ]
});
const lineChartOptions = {
  plugins: {
    legend: { position: 'right' }
  },
  scales: {
    x: { grid: { display: false } },
    y: { beginAtZero: true, ticks: { stepSize: 1 } }
  },
  maintainAspectRatio: false,
  responsive: true
};

const albumsWeekData = ref({
  labels: weekLabels.value,
  datasets: [
    { type: 'line', label: 'Album', data: [0, 0, 0, 0, 0, 0, 0], borderColor: '#22C55E', backgroundColor: 'rgba(34,197,94,0.15)', tension: 0.3, fill: true, pointRadius: 3 }
  ]
});

/* ========== 4) Thu theo kỳ (donut + dropdown) ========== */
const feeOptions = ref([
  { label: 'Học phí Tháng 10', value: 'fee_oct' },
  { label: 'Học phí Tháng 9', value: 'fee_sep' }
]);
const selectedFee = ref(feeOptions.value[0]);

const feeData = ref({
  labels: ['Số đã thu', 'Số còn phải thu'],
  datasets: [
    { data: [0, 0], backgroundColor: ['#3B82F6', '#BFDBFE'], borderWidth: 0 }
  ]
});
const feeOptionsDonut = {
  cutout: '65%',
  plugins: {
    legend: { display: false },
    tooltip: { callbacks: { label: (ctx: any) => `${ctx.label}: ${ctx.parsed}` } }
  },
  maintainAspectRatio: false,
  responsive: true
};

/* ========== 5) Sinh nhật tháng 10 (avatar list demo) ========== */
const birthdaysStudents = ref(Array.from({ length: 11 }, (_, i) => ({ id: i + 1 })));
const birthdaysStaff = ref(Array.from({ length: 11 }, (_, i) => ({ id: i + 100 })));

</script>

<template>
  <div class="p-6 space-y-6">
    <!-- Tiêu đề -->
    <div class="flex flex-wrap justify-between items-center text-surface-900 dark:text-surface-0">
      <h1 class="text-2xl font-semibold">{{ schoolName }}</h1>
      <span class="text-primary font-medium text-lg">Năm học: {{ academicYear }}</span>
    </div>

    <!-- Thẻ thống kê -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-5">
      <div
        v-for="s in stats"
        :key="s.key"
        class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-5 flex items-center gap-4 hover:shadow-md transition-all duration-200"
      >
        <div :class="['w-14 h-14 flex items-center justify-center rounded-xl text-2xl', s.bg, s.fg]">
          <i :class="s.icon"></i>
        </div>
        <div>
          <p class="text-gray-500 dark:text-gray-400 text-sm">{{ s.label }}</p>
          <h3 class="text-3xl font-semibold mt-1">{{ s.value }}</h3>
        </div>
      </div>
    </div>

    <!-- Bảng tin -->
    <div class="flex items-center gap-2 text-surface-700 dark:text-surface-300 mt-8">
      <i class="fa-solid fa-newspaper text-primary text-xl"></i>
      <h2 class="text-xl font-semibold">Bảng tin - {{ todayStr }}</h2>
    </div>

    <!-- Hai cột: Hoạt động | Kiểm duyệt (giữ nguyên) -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- Hoạt động -->
      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6 flex flex-col">
        <header class="flex items-center gap-2 mb-5 border-b border-surface-200 dark:border-surface-700 pb-3">
          <i class="fa-solid fa-chart-line text-primary text-xl"></i>
          <h3 class="text-xl font-semibold">Hoạt động</h3>
        </header>

        <ul class="flex-1 space-y-2">
          <li
            v-for="(item, idx) in [
              { label: 'Thực đơn ngày', value: '3/5', icon: 'fa-solid fa-utensils' },
              { label: 'Bài viết mới', value: '12', icon: 'fa-solid fa-newspaper' },
              { label: 'Album mới', value: '4', icon: 'fa-solid fa-images' },
              { label: 'Phụ huynh đã đăng nhập', value: '18/30', icon: 'fa-solid fa-user-check' },
              { label: 'Học sinh mới trong tháng', value: '6', icon: 'fa-solid fa-user-graduate' }
            ]"
            :key="idx"
            class="flex justify-between items-center bg-surface-50 dark:bg-surface-800 hover:bg-surface-100 dark:hover:bg-surface-700 rounded-xl px-4 py-3 cursor-pointer transition-all duration-200 shadow-sm hover:shadow-md"
          >
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 flex items-center justify-center rounded-lg bg-primary-50 dark:bg-surface-700">
                <i :class="[item.icon, 'text-primary-500 text-base']"></i>
              </div>
              <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ item.label }}</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="font-semibold text-gray-900 dark:text-gray-100">{{ item.value }}</span>
              <i class="fa-solid fa-chevron-right text-gray-400 text-xs"></i>
            </div>
          </li>
        </ul>
      </section>

      <!-- Kiểm duyệt -->
      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6 flex flex-col">
        <header class="flex items-center gap-2 mb-5 border-b border-surface-200 dark:border-surface-700 pb-3">
          <i class="fa-solid fa-list-check text-primary text-xl"></i>
          <h3 class="text-xl font-semibold">Kiểm duyệt</h3>
        </header>

        <ul class="flex-1 divide-y divide-surface-200 dark:divide-surface-700">
          <li
            v-for="(r, idx) in review"
            :key="idx"
            class="flex justify-between items-center py-3 px-2 hover:bg-surface-100 dark:hover:bg-surface-800 rounded-lg transition-all duration-200 cursor-pointer"
          >
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ r.label }}</span>
            <div class="flex items-center space-x-2">
              <span :class="['font-semibold', r.accent]">{{ r.value }}</span>
              <i class="fa-solid fa-angle-right text-gray-400 text-xs"></i>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <!-- Cụm 1: 433 Học sinh | Điểm danh -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
        <header class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold flex items-center gap-2">
            <i class="fa-solid fa-user-graduate"></i>
            {{ studentsCount }} Học sinh
          </h3>
          <i class="fa-solid fa-arrow-right text-primary"></i>
        </header>
        <div class="h-64">
          <Chart type="doughnut" :data="studentStatusData" :options="doughnutOptionsRightLegend" />
        </div>
      </section>

      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
        <header class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold flex items-center gap-2">
            <i class="fa-solid fa-calendar-check"></i>
            Điểm danh {{ studentsCount }} học sinh
          </h3>
          <i class="fa-solid fa-arrow-right text-primary"></i>
        </header>
        <div class="h-64">
          <Chart type="doughnut" :data="attendanceData" :options="doughnutOptionsRightLegend" />
        </div>
      </section>
    </div>

    <!-- Cụm 2: Bài viết tuần | Album tuần -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
        <header class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold flex items-center gap-2">
            <i class="fa-solid fa-book"></i>
            Bài viết tuần
          </h3>
          <i class="fa-solid fa-arrow-right text-primary"></i>
        </header>
        <div class="h-64">
          <Chart :data="postsWeekData" :options="lineChartOptions" />
        </div>
      </section>

      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
        <header class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold flex items-center gap-2">
            <i class="fa-regular fa-image"></i>
            Album tuần
          </h3>
          <i class="fa-solid fa-arrow-right text-primary"></i>
        </header>
        <div class="h-64">
          <Chart :data="albumsWeekData" :options="lineChartOptions" />
        </div>
      </section>
    </div>

    <!-- Cụm 3: Thu theo kỳ | Sinh nhật tháng 10 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
        <header class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-semibold flex items-center gap-2">
            <i class="fa-solid fa-coins"></i>
            Tình hình thực hiện thu theo kỳ
          </h3>
          <div class="w-48">
            <Dropdown v-model="selectedFee" :options="feeOptions" optionLabel="label" class="w-full" />
          </div>
        </header>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
          <div class="md:col-span-1 h-48">
            <Chart type="doughnut" :data="feeData" :options="feeOptionsDonut" />
          </div>
          <div class="md:col-span-2 space-y-6">
            <div>
              <div class="text-surface-500">Tổng số phải thu</div>
              <div class="mt-2 h-px bg-surface-200 dark:bg-surface-700"></div>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="inline-block w-4 h-4 rounded bg-blue-500"></span>
                <span>Số đã thu</span>
              </div>
              <div class="font-semibold">0 đ</div>
            </div>
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <span class="inline-block w-4 h-4 rounded bg-blue-200"></span>
                <span>Số còn phải thu</span>
              </div>
              <div class="font-semibold">0 đ</div>
            </div>
            <div class="font-semibold">Tỷ lệ thu 100%</div>
          </div>
        </div>
      </section>

<section class="rounded-2xl bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-700 p-6">
  <header class="flex items-center justify-between mb-4">
    <h3 class="text-xl font-semibold flex items-center gap-2">
      <i class="fa-solid fa-cake-candles"></i>
      Chúc mừng sinh nhật tháng 10
    </h3>
    <i class="fa-solid fa-arrow-right text-primary"></i>
  </header>

  <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
    <!-- Hai hàng avatar: dùng grid-rows và tăng gap giữa 2 hàng -->
    <div class="md:col-span-2 grid grid-rows-2 gap-y-8">
      <!-- Hàng 1 -->
      <div class="flex flex-wrap gap-x-4 gap-y-4">
        <div
          v-for="s in birthdaysStudents"
          :key="s.id"
          class="w-10 h-10 rounded-full bg-blue-200 flex items-center justify-center"
        >
          <i class="fa-solid fa-user text-blue-700"></i>
        </div>
      </div>
      <!-- Hàng 2 -->
      <div class="flex flex-wrap gap-x-4 gap-y-4">
        <div
          v-for="s in birthdaysStaff"
          :key="s.id"
          class="w-10 h-10 rounded-full bg-blue-200 flex items-center justify-center"
        >
          <i class="fa-solid fa-user text-blue-700"></i>
        </div>
      </div>
    </div>

    <!-- Tổng bên phải -->
    <div class="md:col-span-1 flex flex-col justify-start gap-8">
      <div class="text-right">
        <div class="text-surface-500">Học sinh:</div>
        <div class="text-lg font-semibold">{{ birthdaysStudents.length }}</div>
      </div>
      <div class="text-right">
        <div class="text-surface-500">Nhân viên:</div>
        <div class="text-lg font-semibold">{{ birthdaysStaff.length }}</div>
      </div>
    </div>
  </div>
</section>
    </div>
  </div>
</template>

<style scoped>
.text-surface-900 { color: #1e293b; }
.dark .text-surface-900 { color: #f8fafc; }
</style>
