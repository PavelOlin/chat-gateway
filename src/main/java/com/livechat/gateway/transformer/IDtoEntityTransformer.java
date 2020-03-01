package com.livechat.gateway.transformer;

public interface IDtoEntityTransformer<D, E> {
    E toEntity(D dto);
    D toDto(E entity);
}
