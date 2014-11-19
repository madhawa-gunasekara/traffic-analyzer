<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<!-- This UI is used only for demonstration purposes. Will be replaced by WSO2 CEP Geo-dashboard. -->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta charset="utf-8">
<title>Subscribe | Traffic Analyzer</title>
<style>
html,body {
	height: 100%;
	margin: 0px;
	padding: 0px
}

.labels {
	color: red;
	background-color: white;
	font-family: "Lucida Grande", "Arial", sans-serif;
	font-size: 10px;
	font-weight: bold;
	text-align: center;
	width: 40px;
	border: 2px solid black;
	white-space: nowrap;
}
</style>

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
<!-- Optional: Include the jQuery library -->

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script>
	$(document).ready(function() {
		$('#submit').click(function(event) {
			$("#response").show();
			$('#welcometext').text("Please wait...");
			var emailAdd = $('#email').val();
			var locations = $('#location').val();
			$.get('ActionServlet', {
				user : emailAdd,
				location1 : locations
			}, function(responseText) {
				$('#welcometext').text(responseText);
			});
		});
	});
</script>
</head>

<body>
	<nav class="navbar navbar-inverse navbar-static-top">
		<div class="container">
			<div class="navbar-header">
				<a class="navbar-brand" href="index.html">Traffic Analyzer</a>
			</div>
			<div>
				<ul class="nav navbar-nav">
					<li><a href="index.html">Home</a></li>
					<li><a href="search.html">Search Traffic</a></li>
					<li class="active"><a href="subscribe.jsp">Subscribe</a></li>
				</ul>
			</div>
		</div>
	</nav>
	<div class="container">
		<div class="row">
			<div class="col-md-4">
				<br>
				<form role="form" id="form1">
					<div class="form-group">
						<label for="email">Email:</label> <input type="email"
							class="form-control" id="email">
					</div>
					<div class="form-group">
						<label>Location:</label> <input type="text" class="form-control"
							id="location">
					</div>
					<button type="button" id="submit" class="btn btn-primary">Subscribe</button>
					<br>
					<br>
					<div id="response" class="alert alert-success" role="alert"
						style="display: none">
						<div id="welcometext"></div>
					</div>
				</form>
			</div>
			<div class="col-md-8"></div>
		</div>
	</div>
</body>

</html>