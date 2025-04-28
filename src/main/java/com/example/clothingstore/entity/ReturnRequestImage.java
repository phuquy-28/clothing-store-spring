package com.example.clothingstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "return_request_images")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestImage extends AbstractEntity {

  private String imageUrl;

  @ManyToOne
  @JoinColumn(name = "return_request_id")
  private ReturnRequest returnRequest;
}
