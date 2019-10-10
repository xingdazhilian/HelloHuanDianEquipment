package com.hellohuandian.apps.strategylibrary.dispatchers;

import com.hellohuandian.apps.strategylibrary.strategies._base.TaskStrategy;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-28
 * Description:
 */
abstract class TaskDispatcher<T extends TaskStrategy> extends ConcurrentLinkedQueue<T>
{
    protected abstract void start();

    protected abstract void stop();

    protected abstract void watch(T taskStrategy);

    protected abstract void dispatch(T taskStrategy);
}
