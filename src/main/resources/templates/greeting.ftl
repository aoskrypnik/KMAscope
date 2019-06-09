<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>
    <h5>Hello <#if user??>${name}<#else>guest</#if></h5>
    <div>It is KMAscope</div>
    <img src="static/logo.jpg" alt="100" class="card-img-top"/>
</@c.page>