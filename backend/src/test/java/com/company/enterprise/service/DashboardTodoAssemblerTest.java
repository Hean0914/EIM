package com.company.enterprise.service;

import com.company.enterprise.vo.DashboardTodoVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardTodoAssemblerTest {

    @Test
    void managementTodosKeepUrgentItemsFirstAndSkipEmptyCategories() {
        List<DashboardTodoVO> todos = DashboardTodoAssembler.managementTodos(2, 0, 1, 0, 4);

        assertEquals(List.of("pending-leave", "attendance-anomaly", "upcoming-meeting"),
                todos.stream().map(DashboardTodoVO::getKey).toList());
        assertEquals("high", todos.get(0).getPriority());
        assertEquals(2L, todos.get(0).getCount());
        assertTrue(todos.stream().noneMatch(item -> item.getKey().equals("pending-out")));
    }

    @Test
    void personalTodosDescribeMissingAttendanceAndOverduePlans() {
        List<DashboardTodoVO> todos = DashboardTodoAssembler.personalTodos(0, 0, 1, 3, 2);

        assertEquals(List.of("incomplete-attendance", "overdue-plan", "upcoming-meeting"),
                todos.stream().map(DashboardTodoVO::getKey).toList());
        assertEquals("今日上下班打卡尚未完整", todos.get(0).getTitle());
        assertEquals("/attendance", todos.get(0).getTargetPath());
        assertEquals(3L, todos.get(1).getCount());
    }

    @Test
    void returnsEmptyListWhenThereAreNoTodoCounts() {
        assertTrue(DashboardTodoAssembler.personalTodos(0, 0, 0, 0, 0).isEmpty());
        assertTrue(DashboardTodoAssembler.managementTodos(0, 0, 0, 0, 0).isEmpty());
    }
}
