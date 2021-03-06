package com.kkk.op.support.base;

import com.kkk.op.support.marker.Identifier;

/**
 * Aggregate：Entity的聚合 <br>
 * 聚合根类 marker
 *
 * @author KaiKoo
 */
public abstract class Aggregate<ID extends Identifier> extends Entity<ID> {}
