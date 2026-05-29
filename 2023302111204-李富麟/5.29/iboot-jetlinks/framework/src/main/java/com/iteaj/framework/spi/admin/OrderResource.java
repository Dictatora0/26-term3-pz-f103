package com.iteaj.framework.spi.admin;

public interface OrderResource<T extends OrderResource> extends Resource, Comparable<T> {

    int getSort();

    void setSort(int sort);

    @Override
    default int compareTo(T o) {
        if(this == o) return 0;

        int order = getSort() - o.getSort();
        return order == 0 ? 1 : order;
    }
}
