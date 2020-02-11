package com.hellohuandian.apps.strategylibrary.strategies._base;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-26
 * Description: 节点策略，用来组合有关联关系的执行策略
 */
public abstract class NodeStrategy extends BaseStrategy
{
    private NodeStrategy previous;
    private NodeStrategy next;

    private Consumer<NodeStrategy> nodeStrategyConsumer;

    public NodeStrategy(byte address)
    {
        super(address);
    }

    private void setPrevious(NodeStrategy taskStrategy)
    {
        previous = taskStrategy;
    }

    private void setNext(NodeStrategy taskStrategy)
    {
        next = taskStrategy;
    }

    public NodeStrategy addPrevious(NodeStrategy taskStrategy)
    {
        if (taskStrategy != null)
        {
            taskStrategy.setNext(this);
        }
        return previous == null ? (previous = taskStrategy) : previous.addPrevious(taskStrategy);
    }

    public NodeStrategy addNext(NodeStrategy taskStrategy)
    {
        if (taskStrategy != null)
        {
            taskStrategy.setPrevious(this);
        }
        return next == null ? (next = taskStrategy) : next.addNext(taskStrategy);
    }

    protected NodeStrategy previous()
    {
        return previous;
    }

    public NodeStrategy next()
    {
        return next;
    }

    public void call(Consumer<NodeStrategy> nodeStrategyConsumer)
    {
        if (nodeStrategyConsumer != null)
        {
            this.nodeStrategyConsumer = nodeStrategyConsumer;
            nodeStrategyConsumer.accept(this);
        }
    }

    public void nextCall()
    {
        if (next != null)
        {
            next.call(nodeStrategyConsumer);
        }
    }

    public NodeStrategy first()
    {
        NodeStrategy firstStrategy = this;
        NodeStrategy nodeStrategy;
        while ((nodeStrategy = firstStrategy.previous()) != null)
        {
            firstStrategy = nodeStrategy;
        }
        return firstStrategy;
    }

    public abstract class NodeConsumer implements Consumer<byte[]>
    {
        private boolean isFinishContinue;

        public NodeConsumer()
        {
            this(true);
        }

        public NodeConsumer(boolean isFinishContinue)
        {
            this.isFinishContinue = isFinishContinue;
        }

        @Override
        public void accept(byte[] bytes)
        {
            onAccept(bytes);
            if (isFinishContinue)
            {
                nextCall();
            }
        }

        public abstract void onAccept(byte[] bytes);
    }
}
