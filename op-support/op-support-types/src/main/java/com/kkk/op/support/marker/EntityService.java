package com.kkk.op.support.marker;

import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * domain service 参数使用 @NotNull @NotEmpty 标识
 * @author KaiKoo
 */
public interface EntityService <T extends Entity<ID>, ID extends Identifier>{

    T find(@NotNull ID id);

    void remove(@NotNull T t);

    void save(@NotNull T t);

    List<T> list(@NotEmpty Set<ID> ids);

}
