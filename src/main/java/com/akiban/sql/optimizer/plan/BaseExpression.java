/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.sql.optimizer.plan;

import com.akiban.server.collation.AkCollator;
import com.akiban.server.collation.AkCollatorFactory;
import com.akiban.server.types3.TPreptimeValue;
import com.akiban.sql.optimizer.TypesTranslation;
import com.akiban.server.types.AkType;
import com.akiban.sql.types.CharacterTypeAttributes;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.parser.ValueNode;

/** An evaluated value. 
 * Usually part of a larger expression tree.
*/
public abstract class BaseExpression extends BasePlanElement implements ExpressionNode
{
    private DataTypeDescriptor sqlType;
    private AkType akType;
    private ValueNode sqlSource;
    private TPreptimeValue preptimeValue;

    protected BaseExpression(DataTypeDescriptor sqlType, AkType akType, ValueNode sqlSource) {
        this.sqlType = sqlType;
        this.akType = akType;
        this.sqlSource = sqlSource;
    }

    protected BaseExpression(DataTypeDescriptor sqlType, ValueNode sqlSource) {
        this(sqlType, (sqlType != null) ? TypesTranslation.sqlTypeToAkType(sqlType) : AkType.UNSUPPORTED, sqlSource);
    }

    @Override
    public DataTypeDescriptor getSQLtype() {
        return sqlType;
    }

    @Override
    public AkType getAkType() {
        return akType;
    }

    @Override
    public ValueNode getSQLsource() {
        return sqlSource;
    }

    @Override
    public void setSQLtype(DataTypeDescriptor type) {
        this.sqlType = type;
    }

    @Override
    public AkCollator getCollator() {
        if (sqlType != null) {
            CharacterTypeAttributes att = sqlType.getCharacterAttributes();
            if (att != null) {
                String coll = att.getCollation();
                if (coll != null)
                    return AkCollatorFactory.getAkCollator(coll);
            }
        }
        return null;
    }

    @Override
    public boolean isColumn() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    protected void deepCopy(DuplicateMap map) {
        super.deepCopy(map);
        // Don't clone AST or type.
    }

    @Override
    public TPreptimeValue getPreptimeValue() {
        return preptimeValue;
    }

    @Override
    public void setPreptimeValue(TPreptimeValue value) {
        this.preptimeValue = value;
    }
}
