from app import app, db
from flask import render_template, flash, redirect, url_for, request
from flask_login import current_user, login_user, logout_user, login_required
from app.models import User, Aquarium
from app.email import send_password_reset_email

@app.route('/', methods=['GET', 'POST'])
@app.route('/index', methods=['GET', 'POST'])
@login_required
def index():
	user = current_user;
	aq = user.aquariums.all()
	if request.method == 'POST':
		aquarium = Aquarium(id_aquarium = request.form['id_aquarium'], owner = current_user)
		db.session.add(aquarium)
		db.session.commit()
		return redirect(url_for('index'))
	return render_template('index.html', user=user, aq=aq)

@app.route('/aquariums', methods=['GET','POST'])
@login_required
def aquariums():
	user = current_user;
	aq = user.aquariums.all()
	return render_template('aquariums.html',user=user,aq=aq)

@app.route('/about')
@login_required
def about():
	return render_template('about.html')


@app.route('/login', methods=['GET', 'POST'])
def login():
	if current_user.is_authenticated:
		return redirect(url_for('index'))
	if request.method == 'POST':
		user = User.query.filter_by(username = request.form['username']).first()
		if user is None or not user.check_password(request.form['password']):
			flash('Invalid username or password')
			return redirect(url_for('login'))
		login_user(user)
		return redirect(url_for('index'))
	return render_template('login.html')

@app.route('/reset_password_request', methods=['GET','POST'])
def reset_password_request():
	if current_user.is_authenticated:
		return redirect(url_for('index'))
	if request.method == 'POST':
		user = User.query.filter_by(email=request.form['reset_email']).first()
		if user:
			send_password_reset_email(user)
		return redirect(url_for('login'))
	return render_template('reset_password_request.html')

@app.route('/reset_password/<token>', methods=['GET','POST'])
def reset_password(token):
	if current_user.is_authenticated:
		return redirect(url_for('index'))
	user = User.verify_reset_password_token(token)
	if not user:
		return redirect(url_for('index'))
	if request.method == 'POST':
		user.set_password(request.form['password'])
		db.session.commit()
		return redirect(url_for('login'))
	return render_template('reset_password.html')

@app.route('/logout')
def logout():
	logout_user()
	return redirect(url_for('login'))

@app.route('/register', methods=['GET', 'POST'])
def register():
	if current_user.is_authenticated:
		return redirect(url_for('index'))
	if request.method == 'POST':
		user = User(username = request.form['username'], email = request.form['email'])
		user.set_password(request.form['password'])
		db.session.add(user)
		db.session.commit()
		return redirect(url_for('login'))
	return render_template('register.html')


