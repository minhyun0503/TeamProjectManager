package teamproject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

// [이동주 담당 1] 업무 데이터를 보관하고 진행률과 남은 날짜를 계산합니다.
public class InlineProgressData {

    // 한 명의 팀원 정보를 담는 작은 클래스입니다.
    public static class Person {
        private int userId;
        private String name;

        public Person(int userId, String name) {
            this.userId = userId;
            this.name = name;
        }
        public int getUserId() { return userId; }
        public String getName() { return name; }

        // 이름 선택 상자에 표시할 글자입니다.
        @Override
        public String toString() { return name + " (" + userId + ")"; }
    }

    // 진행률 계산에 필요한 업무 정보 세 가지만 저장합니다.
    public static class Task {
        private int assigneeId;
        private String status;
        private LocalDate dueDate;

        public Task(int assigneeId, String status, LocalDate dueDate) {
            this.assigneeId = assigneeId;
            this.status = status;
            this.dueDate = dueDate;
        }
        public int getAssigneeId() { return assigneeId; }
        public String getStatus() { return status; }
        public LocalDate getDueDate() { return dueDate; }
    }

    // ArrayList는 여러 데이터를 순서대로 담는 목록입니다.
    private ArrayList<Person> people = new ArrayList<Person>();
    private ArrayList<Task> tasks = new ArrayList<Task>();

    public ArrayList<Person> getPeople() { return people; }
    public ArrayList<Task> getTasks() { return tasks; }

    // userId가 0이면 팀 전체, 다른 숫자이면 해당 담당자의 업무만 셉니다.
    // users.user_id는 1부터 시작하므로 0을 '전체' 표시로 사용했습니다.
    public int getTotalCount(int userId) {
        int total = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (userId == 0 || task.getAssigneeId() == userId) {
                total++;
            }
        }
        return total;
    }

    public int getCompletedCount(int userId) {
        int completed = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (userId == 0 || task.getAssigneeId() == userId) {
                if ("완료".equals(task.getStatus())) {
                    completed++;
                }
            }
        }
        return completed;
    }

    public int getPercent(int userId) {
        int total = getTotalCount(userId);
        int completed = getCompletedCount(userId);
        if (total == 0) {
            return 0; // 업무가 없을 때 0으로 나누지 않도록 합니다.
        }
        // 100.0을 써서 소수점까지 계산한 뒤, 정수로 반올림합니다.
        double percent = completed * 100.0 / total;
        return (int) Math.round(percent);
    }

    // 완료한 업무는 마감일이 지나도 지연으로 표시하지 않습니다.
    public static String getDeadline(String status, LocalDate dueDate, LocalDate today) {
        if ("완료".equals(status)) {
            return "완료";
        }
        if (dueDate == null) {
            return "마감일 없음";
        }
        // 오늘부터 마감일까지 며칠 차이인지 구합니다.
        long days = ChronoUnit.DAYS.between(today, dueDate);
        if (days < 0) {
            return "지연 " + (-days) + "일";
        }
        if (days == 0) {
            return "오늘 마감";
        }
        return "D-" + days;
    }
}
