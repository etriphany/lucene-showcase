package com.etriphany.fulltext.domain.io;

/**
 * Defines a content term.
 *
 * @author cadu.goncalves
 *
 */
public class ContentTerm implements Comparable<ContentTerm> {

    // Term value
    private final String text;

    // Term frequency
    private final Integer freq;

    public ContentTerm(String text, Integer freq) {
        this.text = text;
        this.freq = freq;
    }

    public String getText() {
        return text;
    }

    public Integer getFreq() {
        return freq;
    }


    @Override
    public int compareTo(ContentTerm other) {
        int compareTxt = this.text.compareTo(other.getText());
        int compareFreq = Integer.compare(this.freq, other.getFreq());
        return compareTxt + compareFreq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof ContentTerm)) {
            return false;
        }

        ContentTerm that = (ContentTerm) o;

        if (freq != null ? !freq.equals(that.freq) : that.freq != null){
            return false;
        }
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (freq != null ? freq.hashCode() : 0);
        return result;
    }
}
