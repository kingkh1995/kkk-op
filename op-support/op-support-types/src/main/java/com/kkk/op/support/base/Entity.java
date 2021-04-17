package com.kkk.op.support.base;

import com.kkk.op.support.exception.BussinessException;
import com.kkk.op.support.marker.Identifiable;
import com.kkk.op.support.marker.Identifier;

/**
 * Entity：拥有唯一标识和业务行为，尽可能的由DP组成
 * 实体类 marker
 * @author KaiKoo
 */
public abstract class Entity<ID extends Identifier> implements Identifiable<ID> {

    /**
     * 获取快照
     */
    public abstract Object snapshot();

    /**
     * 验证该实体类参数是否合法
     *
     * @throws javax.validation.ValidationException
     */
    public abstract void validate();

    /**
     * 设置id
     */
    protected abstract void setId(ID id);

    @Override
    public void fillInId(ID id) {
        if (this.getId() != null) {
            throw new BussinessException("id已存在不能填补");
        }
        if (id == null) {
            throw new IllegalArgumentException("id不能填补为null");
        }
        this.setId(id);
    }
}
