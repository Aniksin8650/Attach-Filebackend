package com.example.attachfile.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Embeddable
public class LeaveApplicationId implements Serializable {

    private static final long serialVersionUID = 1L; // ✅ fixes the warning

    @Column(name = "emp_id")
    private String empId;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    public LeaveApplicationId() {}

    public LeaveApplicationId(String empId, String startDate, String endDate) {
        this.empId = empId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaveApplicationId)) return false;
        LeaveApplicationId that = (LeaveApplicationId) o;
        return empId.equals(that.empId)
            && startDate.equals(that.startDate)
            && endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return empId.hashCode() + startDate.hashCode() + endDate.hashCode();
    }
}
