package com.warchaser.glsurfaceviewdev.base;

import org.jetbrains.annotations.NotNull;

import java.util.Stack;

public class FragmentStackManager {

    private Stack<FragmentStackBean> mStack;

    private static FragmentStackManager mInstance;

    private FragmentStackManager(){
        mStack = new Stack<>();
    }

    public static FragmentStackManager getInstance(){
        if(mInstance == null){
            synchronized (FragmentStackManager.class){
                if(mInstance == null){
                    mInstance = new FragmentStackManager();
                }
            }
        }

        return mInstance;
    }

    public void pushFragment(FragmentStackBean bean){
        if(isStackNotNull()){
            mStack.push(bean);
        }
    }

    public void popLastFragment(){
        if(isStackAvailable()){
            mStack.pop();
        }
    }

    public void popLastFragments(int amount){
        if(isStackAvailable()){
            for(int i = 0; i < amount; i++){
                mStack.pop();
            }
        }
    }

    public void popAllFragments(){
        if(isStackAvailable()){
            mStack.clear();
        }
    }

    public void popCertainFragment(@NotNull FragmentStackBean bean){
        if(isStackAvailable()){
            final int size = mStack.size();
            for(int i = size - 1; i >= 0; i--){
                FragmentStackBean stackBean = mStack.get(i);
                if(bean.getTag().equals(stackBean.getTag())){
                    mStack.remove(i);
                    break;//认为Fragment是唯一的,只remove顶上一个
                }
            }
        }
    }

    public void popCertainFragment(@NotNull String tag){
        if(isStackAvailable()){
            final int size = mStack.size();
            for(int i = size - 1; i >= 0; i--){
                FragmentStackBean stackBean = mStack.get(i);
                if(tag.equals(stackBean.getTag())){
                    mStack.remove(i);
                    break;//认为Fragment是唯一的,只remove顶上一个
                }
            }
        }
    }

    public FragmentStackBean getCertainFragment(@NotNull FragmentStackBean bean){
        if(isStackAvailable()){
            return mStack.get(mStack.search(bean));
        } else {
            return null;
        }
    }

    public FragmentStackBean getCertainFragment(@NotNull String tag){
        if(isStackAvailable()){
            final int size = mStack.size();
            for(int i = size - 1; i >= 0; i--){
                FragmentStackBean stackBean = mStack.get(i);
                if(tag.equals(stackBean.getTag())){
                    return stackBean;
                }
            }
        }

        return null;
    }

    public boolean isCetainFragmentExist(@NotNull String tag){
        if(isStackAvailable()){
            final int size = mStack.size();
            for(int i = size - 1; i >= 0; i--){
                FragmentStackBean stackBean = mStack.get(i);
                if(tag.equals(stackBean.getTag())){
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isStackAvailable(){
        return isStackNotNull() && !mStack.isEmpty();
    }

    private boolean isStackNotNull(){
        return mStack != null;
    }

}
