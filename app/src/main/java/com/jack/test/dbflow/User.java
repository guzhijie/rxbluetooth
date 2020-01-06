package com.jack.test.dbflow;

import com.jack.dto.annotation.DTO;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/29 0029
 */
@DTO
@Table(database = TestDatabase.class)
public class User {

    @Column
    @PrimaryKey(autoincrement = true)
    private long id;
    @Column
    private String name;

    public long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }
}
