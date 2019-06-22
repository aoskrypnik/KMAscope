<#import "parts/common.ftl" as c>
<#include "parts/security.ftl">

<@c.page>
    <h1>Hello <#if user??>${name}<#else>guest</#if></h1>
    <div><h4>It is KMAscope</h4></div>
    <img src="static/logo.jpg" alt="100" width="400px" height="400px"/>
</@c.page>