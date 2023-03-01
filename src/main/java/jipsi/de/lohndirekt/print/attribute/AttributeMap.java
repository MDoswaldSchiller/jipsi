/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jipsi.de.lohndirekt.print.attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.print.attribute.Attribute;

/**
 *
 * @author mdo
 */
public final class AttributeMap
{
  private final Map<Class<? extends Attribute>,Set<Attribute>> attributes = new HashMap<>();
  
  public boolean containsCategory(Class<? extends Attribute> category)
  {
    return attributes.containsKey(category);
  }
  
  public void put(Attribute attribute)
  {
    var values = attributes.get(attribute.getCategory());
    if (values == null) {
      values = new HashSet<>();
      attributes.put(attribute.getCategory(), values);
    }
    values.add(attribute);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Attribute> Set<T> get(Class<T> category)
  {
    Set<T> set = (Set<T>)attributes.get(category);
    return set != null ? set : Collections.emptySet();
  }
  
  public Iterator<Set<Attribute>> valueIterator()
  {
    return attributes.values().iterator();
  }
  
  public Iterator<Class<? extends Attribute>> keyIterator()
  {
    return attributes.keySet().iterator();
  }
  
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder("AttributeMap{");
    for (Set<Attribute> attrSet : attributes.values()) {
      if (!attrSet.isEmpty()) {
        builder.append('[').append(attrSet.iterator().next().getName()).append('=');
        builder.append(attrSet.stream().map(Attribute::toString).collect(Collectors.joining(",")));
        builder.append("],");
      }
    }
    
    return builder.toString();
  }
}
