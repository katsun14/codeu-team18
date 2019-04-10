package com.google.codeu.data;

public class User {

  private String email;
  private String aboutMe;
  private String name;
  private String country;
  private String language;

  // public User(String email, String aboutMe) {
  //   this.email = email;
  //   this.aboutMe = aboutMe;
  //   this.name = "Unknown User";
  //   this.country = "Unknown Country";
  // }

  public User(String email, String aboutMe, String name, String country, String language) {
    this.email = email;
    this.aboutMe = aboutMe;
    this.name = name;
    this.country = country;
    this.language = language;
  }

  public String getEmail() {
    return email;
  }

  public String getAboutMe() {
    if (aboutMe == null || aboutMe == "") return "Unknown AboutMe";
    return aboutMe;
  }

  public String getName() {
    if (name == null || name == "") return "Unknown User";
    return name;
  }

  public String getCountry() {
    if (country == null || country == "") return "Unknown Country";
    return country;
  }

  public String getLanguage() {
    if (language == null || language == "") {
      return "Unknown Language Preference";
    }

    return language;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
