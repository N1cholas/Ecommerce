<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Document</title>
</head>
<body>
    <h2>test localhost deploy load balance</h2>
    <form action="/user/springsession/login.do" method="post" enctype="multipart/form-data">
        <input name="username" type="text" placeholder="username">
        <input name="password" type="password" placeholder="password">
        <input type="submit">
    </form>

    <h3>springmvc</h3>
    <form action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
        <input type="file" name="file">
        <input type="submit" value="upload">
    </form>

    <h3>simditor</h3>
    <form action="/manage/product/upload_richtxt_img.do" method="post" enctype="multipart/form-data">
        <input type="file" name="file">
        <input type="submit" value="upload">
    </form>
</body>
</html>