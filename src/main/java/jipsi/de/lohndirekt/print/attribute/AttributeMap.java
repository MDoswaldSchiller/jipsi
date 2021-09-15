/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jipsi.de.lohndirekt.print.attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
  
  public Set<Attribute> get(Class<? extends Attribute> category)
  {
    return attributes.get(category);
  }
  
  public Iterator<Set<Attribute>> valueIterator()
  {
    return attributes.values().iterator();
  }
  
  public Iterator<Class<? extends Attribute>> keyIterator()
  {
    return attributes.keySet().iterator();
  }
}
