package com.adflex.profile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeadPayload {

    @JsonProperty("ten_nguoi_gui")
    @NotBlank(message = "Tên người gửi không được để trống")
    private String tenNguoiGui;

    @JsonProperty("sdt")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0[3|5|7|8|9])[0-9]{8}$",
            message = "Số điện thoại không hợp lệ (định dạng VN)"
    )
    private String sdt;

    @JsonProperty("email")
    @Email(message = "Email không hợp lệ")
    private String email;

    @JsonProperty("dia_chi_dn")
    private String diaChiDn;

    @JsonProperty("ten_dn_option_1")
    private String tenDnOption1;

    @JsonProperty("ten_dn_option_2")
    private String tenDnOption2;

    @JsonProperty("ten_dn_option_3")
    private String tenDnOption3;

    @JsonProperty("ten_dn_option_4")
    private String tenDnOption4;

    @JsonProperty("ten_dn_option_5")
    private String tenDnOption5;

    @JsonProperty("nganh_nghe")
    private String nganhNghe;

    @JsonProperty("nhu_cau")
    private String nhuCau;

    // nếu MB Bank có gửi mã ref:
    @JsonProperty("mb_ref_id")
    private String mbRefId;

    // nếu có vốn điều lệ:
    @JsonProperty("charter_capital")
    private Long charterCapital;
}
