<%@ page language="java" contentType="text/html; utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="col-md-12">
    <form class="form-horizontal">
        <div class="content">
            <div class="form-group">
                <label class="col-sm-3 control-label">分类名称:</label>
                <div class="col-sm-8">
                    <input class="form-control" name="CName" type="text" value="${category.CName}"/>
                </div>
            </div>
        </div>
    </form>
</div>

