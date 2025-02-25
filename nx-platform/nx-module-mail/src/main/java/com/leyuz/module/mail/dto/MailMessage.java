package com.leyuz.module.mail.dto;

import lombok.Data;

import java.util.List;

@Data
public class MailMessage {
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String from;
    private String subject;
    private String content;
    private boolean html = false;
}
