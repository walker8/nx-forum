package com.leyuz.bbs.domain.thread;

import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyAttribute;
import com.leyuz.bbs.domain.thread.dataobject.ThreadPropertyTypeV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadPropertyE {
    private Long id;
    private Long threadId;
    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 类型 top digest
     */
    private ThreadPropertyTypeV propertyType;

    /**
     * 类型的属性
     */
    private ThreadPropertyAttribute propertyAttribute;
    /**
     * 操作
     */
    private String operation;
}
