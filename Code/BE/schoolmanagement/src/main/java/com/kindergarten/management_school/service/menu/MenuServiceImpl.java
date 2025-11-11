package com.kindergarten.management_school.service.menu;

import com.kindergarten.management_school.dto.request.MenuRequest;
import com.kindergarten.management_school.dto.request.MenuItemRequest;
import com.kindergarten.management_school.dto.response.*;
import com.kindergarten.management_school.entity.*;
import com.kindergarten.management_school.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenusRepository menusRepository;
    private final MenuItemsRepository menuItemsRepository;
    private final MenuDishRepository menuDishRepository;
    private final ClassesRepository classesRepository;

    @Override
    public ApiResponse<MenuResponse> createWeeklyMenu(MenuRequest request) {
        // ✅ Kiểm tra lớp học tồn tại
        Classes clazz = classesRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp có ID: " + request.getClassId()));

        // ✅ Tạo menu tuần
        Menus menu = Menus.builder()
                .clazz(clazz)
                .startDate(request.getStartDate().atStartOfDay())
                .endDate(request.getEndDate().atTime(23, 59))
                .createdBy(request.getCreatedBy())
                .build();

        // ✅ Tạo các bữa ăn trong tuần
        List<MenuItems> menuItems = request.getItems().stream().map(itemReq -> {
            MenuItems item = MenuItems.builder()
                    .menu(menu)
                    .dayOfWeek(itemReq.getDayOfWeek())
                    .mealType(itemReq.getMealType())
                    .build();

            // ✅ Gắn các món ăn cho bữa
            List<MenuDish> dishes = itemReq.getDishes().stream()
                    .map(dishName -> MenuDish.builder()
                            .menuItems(item)
                            .dishName(dishName)
                            .build())
                    .toList();

            item.setDishes(dishes);
            return item;
        }).toList();

        menu.setMenuItems(menuItems);
        menusRepository.save(menu);

        // ✅ Trả về response
        MenuResponse response = MenuResponse.builder()
                .id(menu.getId())
                .className(clazz.getClassName())
                .classCode(clazz.getClassCode())
                .startDate(menu.getStartDate())
                .endDate(menu.getEndDate())
                .createdBy(menu.getCreatedBy())
                .items(menuItems.stream().map(i ->
                        MenuItemResponse.builder()
                                .dayOfWeek(i.getDayOfWeek())
                                .mealType(i.getMealType())
                                .dishes(i.getDishes().stream()
                                        .map(MenuDish::getDishName)
                                        .collect(Collectors.toList()))
                                .build()
                ).toList())
                .build();

        return ApiResponse.<MenuResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo thực đơn tuần mới thành công cho lớp " + clazz.getClassName())
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<MenuItemResponse> createMenuItem(MenuItemRequest request) {
        try {
            // Tạo MenuItems
            MenuItems menuItem = MenuItems.builder()
                    .dayOfWeek(request.getDayOfWeek())
                    .mealType(request.getMealType())
                    .build();

            List<MenuDish> dishes = request.getDishes().stream()
                    .map(dishName -> MenuDish.builder()
                            .dishName(dishName)
                            .menuItems(menuItem)
                            .build())
                    .collect(Collectors.toList());

            menuItem.setDishes(dishes);

            menuItemsRepository.save(menuItem);

            // Tạo response
            MenuItemResponse response = MenuItemResponse.builder()
                    .dayOfWeek(menuItem.getDayOfWeek())
                    .mealType(menuItem.getMealType())
                    .dishes(
                            menuItem.getDishes().stream()
                                    .map(MenuDish::getDishName)
                                    .collect(Collectors.toList())
                    )
                    .build();

            return ApiResponse.<MenuItemResponse>builder()
                    .status(200)
                    .message("Tạo thực đơn thành công!")
                    .data(response)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<MenuItemResponse>builder()
                    .status(500)
                    .message("Lỗi khi tạo thực đơn: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse<MenuResponse> getWeeklyMenu(Long classId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfWeek = startDate.atStartOfDay();
        LocalDateTime endOfWeek = endDate.atTime(23, 59);

        List<Menus> menus = menusRepository.findMenuByClassAndWeek(classId, startOfWeek, endOfWeek);

        if (menus.isEmpty()) {
            return ApiResponse.<MenuResponse>builder()
                    .status(404)
                    .message("Không có thực đơn cho tuần này!")
                    .data(null)
                    .build();
        }

        Menus menu = menus.get(0);

        MenuResponse response = MenuResponse.builder()
                .id(menu.getId())
                .className(menu.getClazz().getClassName())
                .classCode(menu.getClazz().getClassCode())
                .startDate(menu.getStartDate())
                .endDate(menu.getEndDate())
                .createdBy(menu.getCreatedBy())
                .items(menu.getMenuItems().stream().map(item ->
                        MenuItemResponse.builder()
                                .dayOfWeek(item.getDayOfWeek())
                                .mealType(item.getMealType())
                                .dishes(item.getDishes().stream()
                                        .map(MenuDish::getDishName).toList())
                                .build()
                ).toList())
                .build();

        return ApiResponse.<MenuResponse>builder()
                .status(200)
                .message("Lấy thực đơn tuần thành công!")
                .data(response)
                .build();
    }

    @Override
    public ApiResponse<MenuResponse> updateWeeklyMenu(Long menuId, MenuRequest request) {
        // ✅ Kiểm tra thực đơn có tồn tại không
        Menus existingMenu = menusRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thực đơn có ID: " + menuId));

        // ✅ Kiểm tra lại lớp học (nếu có thay đổi)
        if (!existingMenu.getClazz().getId().equals(request.getClassId())) {
            Classes clazz = classesRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp có ID: " + request.getClassId()));
            existingMenu.setClazz(clazz);
        }

        // ✅ Cập nhật thông tin cơ bản
        existingMenu.setStartDate(request.getStartDate().atStartOfDay());
        existingMenu.setEndDate(request.getEndDate().atTime(23, 59));
        existingMenu.setCreatedBy(request.getCreatedBy());

        // ✅ Xoá toàn bộ các bữa ăn và món cũ (cascade sẽ xoá MenuItems + MenuDish)
        existingMenu.getMenuItems().clear();

        // ✅ Tạo danh sách bữa ăn mới
        List<MenuItems> updatedItems = request.getItems().stream()
                .map(itemReq -> {
                    MenuItems item = MenuItems.builder()
                            .menu(existingMenu)
                            .dayOfWeek(itemReq.getDayOfWeek())
                            .mealType(itemReq.getMealType())
                            .build();

                    // ✅ Gắn các món ăn mới
                    List<MenuDish> dishes = itemReq.getDishes().stream()
                            .map(dishName -> MenuDish.builder()
                                    .menuItems(item)
                                    .dishName(dishName)
                                    .build())
                            .toList();

                    item.setDishes(dishes);
                    return item;
                })
                .toList();

        existingMenu.setMenuItems(updatedItems);
        menusRepository.save(existingMenu);

        // ✅ Tạo response trả về
        MenuResponse response = MenuResponse.builder()
                .id(existingMenu.getId())
                .className(existingMenu.getClazz().getClassName())
                .classCode(existingMenu.getClazz().getClassCode())
                .startDate(existingMenu.getStartDate())
                .endDate(existingMenu.getEndDate())
                .createdBy(existingMenu.getCreatedBy())
                .items(updatedItems.stream().map(i ->
                        MenuItemResponse.builder()
                                .dayOfWeek(i.getDayOfWeek())
                                .mealType(i.getMealType())
                                .dishes(i.getDishes().stream()
                                        .map(MenuDish::getDishName)
                                        .toList())
                                .build()
                ).toList())
                .build();

        return ApiResponse.<MenuResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật thực đơn tuần thành công!")
                .data(response)
                .build();
    }

}
