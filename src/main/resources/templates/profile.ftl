<#import "parts/common.ftl" as c>

<@c.page>
    <h5>${username}, change something if you want</h5>
    ${message!}
    <form method="post">
        <div class="form-group row">
            <label class="col-sm-2 col-form-label">Password</label>
            <div class="col-sm-6">
                <input type="text" name="password" class="form-control" placeholder="Password"
                value="${password!''}"/>
            </div>
        </div>
        <div class="form-group row">
            <label class="col-sm-2 col-form-label">Email</label>
            <div class="col-sm-6">
                <input type="email" name="email" class="form-control" placeholder="email@ukma.edu.ua"
                value="${email!''}"/>
            </div>
        </div>
        <input type="hidden" name="_csrf" value="${_csrf.token}">
        <button class="btn btn-primary" type="submit">Save</button>
    </form>
</@c.page>