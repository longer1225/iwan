package com.iwan.blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {

    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;
    private Object pageExt;

    public static <T> PageVO<T> of(Long total, Long pages, Long current, Long size, List<T> records) {
        PageVO<T> page = new PageVO<>();
        page.setTotal(total);
        page.setPages(pages != null ? pages.intValue() : null);
        page.setCurrent(current != null ? current.intValue() : null);
        page.setSize(size != null ? size.intValue() : null);
        page.setRecords(records);
        return page;
    }
}
