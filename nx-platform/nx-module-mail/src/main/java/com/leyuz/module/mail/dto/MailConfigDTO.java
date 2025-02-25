package com.leyuz.module.mail.dto;

import lombok.Data;

@Data
public class MailConfigDTO {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String defaultFrom;
    private Boolean auth = true;
    private Boolean starttlsEnable = true;
    private String protocol = "smtps";
}
