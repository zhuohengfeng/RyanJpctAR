package com.ryan.ryanjpctar.vuforia.utils;

import com.threed.jpct.Object3D;

import java.util.List;

/**
 * 模型类
 * 存放模型的一些属性
 *
 * @author Admin
 */
public class ObjItem {
    /**
     * 模型的索引
     * 在这里我用追踪的文件名（上传到Vuforia的图片名）做索引
     */
    private String index;
    /**
     * 要加载的obj文件
     */
    private String obj;
    /**
     * 要加载的mtl文件
     */
    private String mtl;
    /**
     * 该模型的所有纹理
     */
    private List<String> textures;

    private List<String> texturesName;

    private String ser;

    public String getSer() {
        return ser;
    }

    public void setSer(String ser) {
        this.ser = ser;
    }

    /* private float postScale = 1;
      // 0 度躺著正面朝上
      private float rotateX = 0;
      private float rotateY = 0;
      private float rotateZ = 0;
      private float traslateX = 0;
      private float traslateY = 0;
      private float traslateZ = 0;*/

    /**
     * obj3D（就是当前模型）
     */
    private Object3D target;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getMtl() {
        return mtl;
    }

    public void setMtl(String mtl) {
        this.mtl = mtl;
    }

    public List<String> getTextures() {
        return textures;
    }

    public void setTextures(List<String> textures) {
        this.textures = textures;
    }

    public Object3D getTarget() {
        return target;
    }

    public void setTarget(Object3D target) {
        this.target = target;
    }

    public List<String> getTexturesName() {
        return texturesName;
    }

    public void setTexturesName(List<String> texturesName) {
        this.texturesName = texturesName;
    }
}
