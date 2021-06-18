import re
from flask import Flask, json, jsonify
from flask.globals import request
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import func
from sqlalchemy.orm import backref, relationship, session 

app = Flask(__name__)

app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://mlupjewpyiktea:3c2ac527dae4079780221aa43f877e730e098169bbb22419959996529dd6539b@ec2-54-74-156-137.eu-west-1.compute.amazonaws.com:5432/dalap036n3t4gb'

db = SQLAlchemy(app)

def checkNone(relationship):
    if(not relationship.correct_answers):
        relationship.correct_answers = "None"
    if(not relationship.easiness_factor):
        relationship.easiness_factor = "None"
    if(not relationship.repetition_interval):
        relationship.repetition_interval = "None"
    if(not relationship.next_date):
        relationship.next_date = "None"
    return relationship

class Category(db.Model):
    __tablename__ = "category"
    category_id = db.Column(db.Integer, primary_key=True)
    category_name = db.Column(db.String(30), nullable=False)
    cards = db.relationship("Card", backref="category", lazy=True)
class Card(db.Model):
    __tablename__ = "card"
    card_id = db.Column(db.Integer, primary_key=True)
    question = db.Column(db.String(200), nullable=False)
    answer = db.Column(db.String(200), nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey("category.category_id"), nullable=False)
    user_cards = db.relationship("UserCard", backref="card", lazy=True)
class User(db.Model):
    __tablename__ = "user"
    username = db.Column(db.String(30), primary_key=True)
    email = db.Column(db.String(50), nullable=False)
    password = db.Column(db.String(30), nullable=False)
    user_cards = db.relationship("UserCard", backref="user", lazy=True)
class UserCard(db.Model):
    __tablename__ = "user_card"
    card_id = db.Column(db.Integer, db.ForeignKey("card.card_id"), primary_key=True)
    username = db.Column(db.String(30), db.ForeignKey("user.username"), primary_key=True)
    correct_answers = db.Column(db.Integer, nullable=True)
    easiness_factor = db.Column(db.Float, nullable=True)
    repetition_interval = db.Column(db.Integer, nullable=True)
    next_date = db.Column(db.String(20), nullable=True)


@app.route("/categories")
def get_categories():
    categories = Category.query.order_by(Category.category_id).all()
    string = ""
    json_categories = []
    for category in categories:
        string += str(category.category_id) + "#" + category.category_name + ";"
        json = jsonify(id = category.category_id,
            category = category.category_name
            )
        json_categories.append(json)
    string = string[:-1]
    return string

@app.route("/getcards/<category>/<user>")
def get_cards(category,user):
    requested_category = Category.query.filter_by(category_name=category).first()
    category_id = requested_category.category_id
    cards = Card.query.filter_by(category_id=category_id)
    string = ""
    for card in cards:
        relationship = UserCard.query.filter_by(card_id=card.card_id, username=user).first()
        if not relationship.correct_answers:
            string += str(card.card_id) + "#" + user + "#" + card.question + "#" + card.answer + "#" + str(card.category_id) + "#None#None#None#None" + ";"
        else:
            string += str(card.card_id) + "#" + user + "#" + card.question + "#" + card.answer + "#" + str(card.category_id) + "#" + str(relationship.correct_answers) + "#" + str(relationship.easiness_factor) + "#" + str(relationship.repetition_interval) + "#" + relationship.next_date + ";"
    string = string[:-1]
    return string

@app.route("/getRelationship/<user>/<card>")
def get_Relationship(user, card):
    relationship = UserCard.query.filter_by(card_id=card, username=user).first()
    relationship = checkNone(relationship)
    string = str(relationship.card_id) + "#" + relationship.username + "#" + str(relationship.correct_answers) + "#" + str(relationship.easiness_factor) + "#" + str(relationship.repetition_interval) + "#" + relationship.next_date
    return string

@app.route("/updateRelationship/<user>/<card>/<correct>/<ef>/<interval>/<date>")
def update_Relationship(user, card, correct, ef, interval, date):
    card_id = int(card)
    relationship = UserCard.query.filter_by(card_id=card_id, username=user).first()
    relationship.correct_answers = int(correct)
    relationship.easiness_factor = float(ef)
    relationship.repetition_interval = int(interval)
    relationship.next_date = date
    db.session.commit()
    return "relationship updated"


@app.route("/createRelationship/<user>/<card>")
def create_Relationship(user, card):
    card_id = int(card)
    relationship = UserCard(card_id = card_id, username = user)
    db.session.add(relationship)
    db.session.commit()
    return "Added relationship for: " + user + "->#" + card

@app.route("/checkRelationships/<user>/<category>")
def check_Relationships(user, category):
    requested_category = Category.query.filter_by(category_name=category).first()
    category_id = requested_category.category_id
    card = Card.query.filter_by(category_id=category_id).first()
    relationship = UserCard.query.filter_by(card_id=card.card_id, username=user).first()
    if relationship:
        return "True"
    else:
        return "False"

@app.route("/createRelationships/<user>/<category>")
def create_Relationships(user, category):
    requested_category = Category.query.filter_by(category_name=category).first()
    category_id = requested_category.category_id
    cards = Card.query.filter_by(category_id=category_id)
    for card in cards:
        relationship = UserCard(card_id = card.card_id, username = user)
        db.session.add(relationship)
    db.session.commit()
    return "Added relationships for: " + user + "->#" + category

@app.route("/addUser", methods=["POST"])
def add_user():
    username = request.form.get("username")
    email = request.form.get("email")
    password = request.form.get("password")
    user = User(username=username, email=email, password=password)
    db.session.add(user)
    db.session.commit()
    return username + "#" + email + "#" + password

@app.route("/getUsers")
def get_users():
    users = User.query.order_by(User.username).all()
    string = ""
    for user in users:
        string += user.username + "#" + user.email + "#" + user.password + ";"
    string = string[:-1]
    return string

@app.route("/getCardStatistics/<card>/<user>")
def get_card_statistics(card, user):
    userCards = UserCard.query.filter(UserCard.card_id==card, UserCard.username!=user, UserCard.easiness_factor.isnot(None))
    string = ""
    for card in userCards:
        string += str(card.easiness_factor) + "#"
    string = string[:-1]
    return string

@app.route("/getCategoryPoints/<category>/<user>")
def get_category_statistics(category, user):
    sums = db.session.query(func.sum(UserCard.easiness_factor)).join(Card).filter(UserCard.username!=user).filter(Card.category_id==category).group_by(UserCard.username)
    string = ""
    for sum in sums:
        if "None" in str(sum):
            continue
        string += str(sum)[1:6] + "#"
    string = string[:-1]
    return string

@app.route("/saveProgress", methods=["POST"])
def save_progress():
    username = request.form.get("username")
    progress = request.form.get("progress")
    cards = progress.split(";")
    counter = 0
    for card in cards:
        data = card.split("#")
        relationship = UserCard.query.filter_by(card_id=data[0], username=username).first()
        relationship.correct_answers = int(data[1])
        relationship.easiness_factor = float(data[2])
        relationship.repetition_interval = int(data[3])
        relationship.next_date = data[4]
        db.session.commit()
        counter += 1
    
    return str(counter)


if __name__ == "__main__":
    app.run(host='0.0.0.0')
